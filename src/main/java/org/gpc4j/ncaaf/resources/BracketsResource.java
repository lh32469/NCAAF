package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.jaxb.Bracket;
import org.gpc4j.ncaaf.jaxb.Conference;
import org.gpc4j.ncaaf.jaxb.Division;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
@Api(value = "Brackets")
@Path("brackets")
public class BracketsResource {

    private Jedis jedis;

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(BracketsResource.class);

    @Inject
    private GamesProvider gp;

    @Inject
    private JedisPool pool;


    @PostConstruct
    public void postConstruct() {
        jedis = pool.getResource();
        jedis.select(10);
        LOG.debug("Jedis: " + jedis);
    }


    @PreDestroy
    public void preDestroy() {
        LOG.debug("Jedis: " + jedis);
        pool.returnResource(jedis);
    }


    @GET
    @Timed
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Bracket get() {
        Bracket bracket = new Bracket();
        Set<String> confs = jedis.keys("*.conf");

        for (String conf : confs) {
            Conference conference = new Conference();
            conference.setName(conf.replaceAll("\\.conf", ""));
            bracket.getConference().add(conference);

            Map<String, Division> divisions = new HashMap<>();
            Map<String, String> data = jedis.hgetAll(conf);

            for (Map.Entry<String, String> entry : data.entrySet()) {
                String team = entry.getKey();
                String divisionName = entry.getValue();

                Division div;
                if (divisions.containsKey(divisionName)) {
                    div = divisions.get(divisionName);
                } else {
                    div = new Division();
                    div.setName(divisionName);
                    conference.getDivision().add(div);
                    divisions.put(divisionName, div);
                }

                Team t = new Team();
                t.setName(team);

                div.getTeam().add(t);

            }

            // Sort Divisions
            for (Division div : conference.getDivision()) {
                div.getTeam().stream().forEach(team -> {

                    List<Game> confGames = gp.getGames()
                            .filter(GamesResource.inConference(conference))
                            .collect(Collectors.toList());

                    long conferenceWins = confGames.stream()
                            .filter(GamesResource.win(team.getName()))
                            .count();

                    long conferenceLosses = confGames.stream()
                            .filter(GamesResource.played(team.getName()))
                            .filter(GamesResource.loss(team.getName()))
                            .count();

                    team.setWins((int) conferenceWins);
                    team.setLosses((int) conferenceLosses);
                });
                Collections.sort(div.getTeam(), new TeamSorter());
            }

        }

        return bracket;
    }


    @GET
    @Timed
    @Path("leaders")
    @ApiOperation(
            value = "Get the leaders for the Divisions of every Conference",
            response = Bracket.class)
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Bracket getLeaders() {

        Bracket bracket = get();

        bracket.getConference().forEach(conf -> {
            conf.getDivision().forEach(div -> {
                Team leader = div.getTeam().get(0);
                div.getTeam().clear();
                div.getTeam().add(leader);
            });

        });

        return bracket;
    }


    private class TeamSorter implements Comparator<Team> {

        @Override
        public int compare(Team team1, Team team2) {
            int result = team2.getWins() - team1.getWins();
            if (result == 0) {
                // Same amount of wins, compare losses
                result = team1.getLosses() - team2.getLosses();
            }
            return result;
        }


    }

}
