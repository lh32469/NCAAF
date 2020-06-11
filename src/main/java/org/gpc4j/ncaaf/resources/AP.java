package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.TeamProvider;
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

    @Inject
    private JedisPool pool;

    @Inject
    private GamesProvider gp;

    @Inject
    private TeamProvider tp;

    @Inject
    AP_View view;

    @PostConstruct
    public void postConstruct() {
        jedis = pool.getResource();
        LOG.debug("Jedis: " + jedis);
    }


    @PreDestroy
    public void preDestroy() {
        LOG.debug("Jedis: " + jedis);
        pool.returnResource(jedis);
    }


    @GET
    @Timed
    @Path("{year}")
    public AP_View getYear(@PathParam("year") Integer year) throws Exception {
        LOG.info(year.toString());

        view.setTitle(year + " AP Rankings");
        view.setWeeks(getWeeks(year).collect(Collectors.toList()));
        view.setYear(year);

        return view;
    }


    Stream<Week> getWeeks(int year) throws InterruptedException,
            ExecutionException {
        LOG.debug(year + "");
        final LinkedList<Week> weeks = new LinkedList<>();
        final List<Future<Week>> futures = new LinkedList<>();

        final int numWeeks = 18;

        // Submit all requests
        for (int i = 0; i < numWeeks; i++) {
            String key = "AP." + year + "." + i;
            futures.add(new GetWeekCommand(key, pool, tp).queue());
        }

        int xPosition = 0;

        // Collect the data
        for (int i = 0; i < numWeeks; i++) {
            xPosition += 250;
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

        if (!weeks.isEmpty()) {
            // Get next weeks opponent
            Week thisWeek = weeks.getLast();

            List<Game> games = gp.getGames().collect(Collectors.toList());

            for (Team team : thisWeek.getTeams()) {

                Optional<Game> nextGame = getNextGame(games, team.getName());
                if (nextGame.isPresent()) {
                    LOG.debug("NextGame:  "
                            + team.getName() + " -> "
                            + nextGame.get());
                    team.setNextGame(nextGame.get());
                } else {
                    LOG.debug("NextGame:  " + team.getName() + " -> None");
                }

            }
        }

        return weeks.parallelStream();
    }


    Optional<Game> getNextGame(List<Game> games, String teamName) {

        Optional<Game> next = Optional.empty();

        for (Game game : games) {
            if (game.getHomeScore() == null) {
                // Game not played yet
                if (teamName.equals(game.getHome())
                        || teamName.equals(game.getVisitor())) {
                    next = Optional.of(game);
                    break;
                }
            }
        }

        return next;
    }


}
