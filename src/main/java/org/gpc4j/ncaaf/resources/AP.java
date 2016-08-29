package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.apache.commons.lang.StringUtils;
import org.gpc4j.ncaaf.XGame;
import org.gpc4j.ncaaf.hystrix.GetGameCommand;
import org.gpc4j.ncaaf.hystrix.GetTeamCommand;
import org.gpc4j.ncaaf.hystrix.GetWeekCommand;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.jaxb.Week;
import org.gpc4j.ncaaf.views.AP_View;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 * AP_View Top 25.
 *
 * @author Lyle T Harris
 */
@Path("{parameter: ap|AP}")
public class AP {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP.class);

    private Jedis jedis;

    private List<Future<Game>> games;

    @Inject
    private JedisPool pool;


    @PostConstruct
    public void init() {
        jedis = pool.getResource();
        jedis.select(10);
        LOG.debug("Jedis: " + jedis);

        // Load Games in the background.
        games = new LinkedList<>();
        for (String key : jedis.keys("game.2016.*")) {
            games.add(new GetGameCommand(key, pool).queue());
        }
    }


    @PreDestroy
    public void destroy() {
        LOG.debug("Jedis: " + jedis);
        pool.returnResource(jedis);
    }


    @GET
    @Timed
    @Path("{year}")
    public AP_View getYear(@PathParam("year") Integer year) throws Exception {
        LOG.info(year.toString());
        //LOG.debug("This: " + this);
        //LOG.debug("Pool: " + pool);

        AP_View view = new AP_View();
        view.setTitle(year + " AP Rankings");
        view.setWeeks(getWeeks(year).collect(Collectors.toList()));

        return view;
    }


    @Timed
    Stream<Week> getWeeks(int year) throws InterruptedException,
            ExecutionException {
        LOG.info(year + "");
        final LinkedList<Week> weeks = new LinkedList<>();
        final List<Future<Week>> futures = new LinkedList<>();

        final int numWeeks = 18;

        // Submit all requests
        for (int i = 0; i < numWeeks; i++) {
            String key = "AP." + year + "." + i;
            futures.add(new GetWeekCommand(key, pool).queue());
        }

        int xPosition = 0;

        // Collect the data
        for (int i = 0; i < numWeeks; i++) {
            xPosition += 200;
            Week w = futures.get(i).get();
            if (w.getTeams().isEmpty()) {
                break;
            }
            w.setNumber(i);
            w.setXPos(xPosition);
            final int pos = xPosition;
            w.getTeams().parallelStream().forEach((team) -> {
                team.setCX(pos);
            });
            weeks.add(w);
        }

        // Get next weeks opponent
        Week thisWeek = weeks.getLast();

        // Fetch the loaded Games
        List<Game> _games = new LinkedList<>();
        for (Future<Game> game : games) {
            _games.add(game.get());
        }

        for (Team team : thisWeek.getTeams()) {
            Optional<Team> next = getNext(_games, team.getName());
            if (next.isPresent()) {
                LOG.info("Next:  "
                        + team.getName() + " -> "
                        + next.get().getName());
                team.setNext(next.get());
            }
        }

        return weeks.parallelStream();
    }


    @Timed
    Optional<Team> getNext(List<Game> games, String teamName) {

        Optional<Team> next = Optional.empty();

        for (Game game : games) {

            String home = game.getHome();
            String visitor = game.getVisitor();

            // LOG.info(visitor + "@" + home);
            if (teamName.equals(home)) {
                LOG.info(visitor + "@" + teamName);
                next = Optional.of(new GetTeamCommand(visitor, jedis).execute());
            } else if (teamName.equals(visitor)) {
                LOG.info(teamName + "@" + home);
                next = Optional.of(new GetTeamCommand(home, jedis).execute());
            }
        }

        return next;
    }


}
