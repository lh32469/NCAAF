package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

        List<Game> games = gp.getGames()
                .filter((game) -> !Strings.isNullOrEmpty(game.getHomeScore()))
                .filter((game) -> !Strings.isNullOrEmpty(game.getDate()))
                .filter((game) -> game.getDate().contains(year.toString()))
                .collect(Collectors.toList());

        games.forEach((g) -> {
            g.setId("");
        });

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("team/{team}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getAllGamesPlayedForTeam(@PathParam("team") String team) {

        List<Game> games = gp.getGames()
                .filter((game) -> !Strings.isNullOrEmpty(game.getHomeScore()))
                .filter((game) -> game.getHome().equals(team)
                        || game.getVisitor().equals(team))
                .collect(Collectors.toList());

        games.forEach((g) -> {
            g.setId("");
        });

        LOG.debug(team + ": " + games.size());
        Games g = new Games();
        g.getGame().addAll(games);

        return g;
    }


    @GET
    @Timed
    @Path("team/{team}/{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getGamesPlayedForTeamByYear(
            @PathParam("year") Integer year,
            @PathParam("team") String team) {

        Games g = getAllGamesPlayedForTeam(team);
        Iterator<Game> games = g.getGame().iterator();
        while (games.hasNext()) {
            Game game = games.next();
            if (Strings.isNullOrEmpty(game.getDate())) {
                games.remove();
            } else if (!game.getDate().contains(year.toString())) {
                games.remove();
            } else if (Strings.isNullOrEmpty(game.getHomeScore())) {
                // Game not played yet.
                games.remove();
            }
        }

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

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

        Games g = getGamesPlayedForTeamByYear(year, team);

        Iterator<Game> games = g.getGame().iterator();

        while (games.hasNext()) {
            Game game = games.next();

            int hScore = Integer.parseInt(game.getHomeScore());
            int vScore = Integer.parseInt(game.getVisitorScore());
            if (game.getHome().equals(team) && hScore < vScore) {
                games.remove();
            } else if (game.getVisitor().equals(team) && vScore < hScore) {
                games.remove();
            }
        }

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

        Games g = getWinsForTeamByYear(year, team);

        Iterator<Game> games = g.getGame().iterator();

        while (games.hasNext()) {
            Game game = games.next();
            if (game.getHome().equals(team)
                    && Strings.isNullOrEmpty(game.getVisitorRank())) {
                games.remove();
            } else if (game.getVisitor().equals(team)
                    && Strings.isNullOrEmpty(game.getHomeRank())) {
                games.remove();
            }

        }

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

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

        Games g = getGamesPlayedForTeamByYear(year, team);
        Games wins = getWinsForTeamByYear(year, team);

        g.getGame().removeAll(wins.getGame());

        LOG.debug(year.toString() + "/" + team + ": " + g.getGame().size());

        return g;
    }


}
