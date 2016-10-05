package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Games;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
@Path("games")
public class GamesResource {

    final Predicate<String> empty = s -> Strings.isNullOrEmpty(s);


    /**
     * Check if Game was played in the year provided.
     *
     * @param year
     * @return
     */
    private static Predicate<Game> played(Integer year) {
        return g -> !Strings.isNullOrEmpty(g.getHomeScore())
                && !Strings.isNullOrEmpty(g.getDate())
                && g.getDate().contains(year.toString());
    }


    /**
     * Check if Game was a win for the team name provided.
     *
     * @param teamName
     * @return
     */
    private static Predicate<Game> win(String teamName) {
        return g -> {

            if (!finished().test(g)) {
                return false;
            }

            int hScore = Integer.parseInt(g.getHomeScore());
            int vScore = Integer.parseInt(g.getVisitorScore());

            if (g.getHome().equals(teamName) && hScore > vScore) {
                return true;
            } else if (g.getVisitor().equals(teamName) && vScore > hScore) {
                return true;
            }

            return false;
        };
    }


    /**
     * Check if Game was a loss for the team name provided.
     *
     * @param teamName
     * @return
     */
    private static Predicate<Game> loss(String teamName) {
        return g -> finished().test(g) && !win(teamName).test(g);
    }


    /**
     * Check if Game was against a then ranked opponent.
     *
     * @param teamName
     * @return
     */
    private static Predicate<Game> rankedOpponent(String teamName) {
        return game -> {

            if (game.getHome().equals(teamName)
                    && !Strings.isNullOrEmpty(game.getVisitorRank())) {
                return true;
            } else if (game.getVisitor().equals(teamName)
                    && !Strings.isNullOrEmpty(game.getHomeRank())) {
                return true;
            }

            return false;
        };
    }


    private static Predicate<Game> finished() {
        return g -> !Strings.isNullOrEmpty(g.getHomeScore());
    }


    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GamesResource.class);

    @Inject
    private GamesProvider gp;


    @GET
    @Timed
    @Path("year/{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getAllGamesPlayedForYear(@PathParam("year") Integer year) {

        final List<Game> games = gp.byYear(year)
                .filter(finished())
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("{team}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getAllGamesPlayedForTeam(@PathParam("team") String team) {

        List<Game> games = gp.byTeam(team)
                .filter(finished())
                .collect(Collectors.toList());

        LOG.debug(team + ": " + games.size());
        Games g = new Games();
        g.getGame().addAll(games);

        return g;
    }


    @GET
    @Timed
    @Path("{team}/{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getGamesPlayedForTeamByYear(
            @PathParam("year") Integer year,
            @PathParam("team") String team) {

        List<Game> games = gp.byTeamAndYear(team, year)
                .filter(finished())
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("wins/{team}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getWinsForTeam(@PathParam("team") String team) {

        List<Game> games = gp.byTeam(team)
                .filter(win(team))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(team + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("wins/{team}/{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getWinsForTeamByYear(
            @PathParam("year") Integer year,
            @PathParam("team") String team) {

        List<Game> games = gp.byTeamAndYear(team, year)
                .filter(win(team))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("wins/ranked/{team}/{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getWinsForTeamByYearRanked(
            @PathParam("year") Integer year,
            @PathParam("team") String team) {

        List<Game> games = gp.byTeamAndYear(team, year)
                .filter(win(team).and(rankedOpponent(team)))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("wins/ranked/{team}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getWinsForTeamRanked(@PathParam("team") String team) {

        List<Game> games = gp.byTeam(team)
                .filter(win(team))
                .filter(rankedOpponent(team))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(team + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("losses/{team}/{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getLossesForTeamByYear(
            @PathParam("year") Integer year,
            @PathParam("team") String team) {

        List<Game> games = gp.byTeamAndYear(team, year)
                .filter(finished())
                .filter(loss(team))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

        return g;
    }


}
