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
import org.gpc4j.ncaaf.views.CFB_View;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 * AP_View Top 25.
 *
 * @author Lyle T Harris
 */
@Path("{parameter: cfb|CFB|cfp|CFP}")
public class CFB extends NCAAF {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(CFB.class);

    private Jedis jedis;


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
    public CFB_View getYear(@PathParam("year") Integer year) throws
            InterruptedException, ExecutionException {

        LOG.info(year.toString());

        CFB_View view = new CFB_View(year);
        view.setTitle(year + " College Football Playoff Rankings");
        view.setWeeks(getWeeks("cfb", year).collect(Collectors.toList()));
        view.setTp(tp);
        view.setGp(gp);
        
        return view;
    }


    Stream<Week> getWeeks(final String poll, int year) throws
            InterruptedException, ExecutionException {

        LOG.debug(year + "");
        final LinkedList<Week> weeks = new LinkedList<>();
        final List<Future<Week>> futures = new LinkedList<>();

        final int numWeeks = 18;

        // Submit all requests
        for (int i = 9; i < numWeeks; i++) {
            String key = poll.toUpperCase() + "." + year + "." + i;
            futures.add(new GetWeekCommand(key, pool, tp).queue());
        }

        int xPosition = 0;

        // Collect the data
        int weekStart = 9;
        for (Future<Week> future : futures) {
            xPosition += 250;
            Week w = future.get();
            if (w.getTeams().isEmpty()) {
                break;
            }
            w.setNumber(weekStart++);
            w.setXPos(xPosition);
            final int pos = xPosition;
            w.getTeams().parallelStream().forEach((team) -> {
                team.setCX(pos);
            });
            weeks.add(w);

        }

        // Get next weeks opponent
        Week thisWeek = weeks.getLast();

        List<Game> games = gp.getGames().collect(Collectors.toList());

        for (Team team : thisWeek.getTeams()) {

            Optional<Team> next = getNext(games, team.getName());
            if (next.isPresent()) {
                LOG.debug("Next:  "
                        + team.getName() + " -> "
                        + next.get().getName());
                team.setNext(next.get());
            }

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

        return weeks.parallelStream();
    }


}
