package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.gpc4j.ncaaf.providers.GamesProvider;
import org.gpc4j.ncaaf.jaxb.Conference;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Games;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
@Path("games")
@Api(value = "Games", description = "Operations about games")
public class GamesResource {

    final Predicate<String> empty = s -> Strings.isNullOrEmpty(s);


    /**
     * Check if Game was played in the year provided.
     *
     * @param year
     * @return
     */
    public static Predicate<Game> played(Integer year) {
        return g -> !Strings.isNullOrEmpty(g.getHomeScore())
                && !Strings.isNullOrEmpty(g.getDate())
                && g.getDate().contains(year.toString());
    }


    /**
     * Check if Game played involved the named Team.
     *
     * @param teamName
     * @return
     */
    public static Predicate<Game> played(String teamName) {
        return g -> {
            return g.getHome().equals(teamName)
                    || g.getVisitor().equals(teamName);
        };
    }


    /**
     * Check if Game was a win for the team name provided.
     *
     * @param teamName
     * @return
     */
    public static Predicate<Game> win(String teamName) {
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
     * Check if Game is in the Conference provided.
     *
     * @param conference
     * @return
     */
    public static Predicate<Game> inConference(Conference conference) {

        return (Game game) -> {

            // Collect all Team names
            final List<String> teamNames = new LinkedList<>();
            conference.getDivision().stream().forEach((division) -> {
                division.getTeam().stream().forEach((team) -> {
                    teamNames.add(team.getName());
                });
            });

            return teamNames.contains(game.getHome())
                    && teamNames.contains(game.getVisitor());

        };
    }


    /**
     * Check if Game was a loss for the team name provided.
     *
     * @param teamName
     * @return
     */
    public static Predicate<Game> loss(String teamName) {
        return g -> finished().test(g) && !win(teamName).test(g);
    }


    /**
     * Check if Game was against a then ranked opponent.
     *
     * @param teamName
     * @return
     */
    public static Predicate<Game> rankedOpponent(String teamName) {
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


    public static Predicate<Game> finished() {
        return g -> !Strings.isNullOrEmpty(g.getHomeScore());
    }


    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GamesResource.class);

    @Inject
    private GamesProvider gp;


    @GET
    @Timed
    @Path("year/{year}")
    public Games getAllGamesPlayedForYear(@PathParam("year") Integer year) {

        final List<Game> games = gp.playedByYear(year)
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        LOG.debug(year.toString() + ": " + g.getGame().size());

        return g;
    }


    @GET
    @Timed
    @Path("{team}")
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
    @Path("points/{team}")
    public String points(@PathParam("team") String team) {

        Integer total = gp.byTeam(team)
                .filter(finished())
                .map(g -> {
                    if (g.getHome().equals(team)) {
                        return Integer.parseInt(g.getHomeScore());
                    } else {
                        return Integer.parseInt(g.getVisitorScore());
                    }
                })
                .mapToInt(Integer::new)
                .sum();

        LOG.debug(team + ": " + total);

        return total.toString();
    }


    // <editor-fold defaultstate="collapsed" desc="Annotations">
    @ApiOperation(
            value = "Get the 'best' game for the team provided",
            notes = "Best game is currently defined as the game won "
            + "by the biggest margin.",
            response = Game.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = Game.class),
        @ApiResponse(code = 404, message = "Game not found")})
    @GET
    @Timed
    @Path("best/{team}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    // </editor-fold>
    public Response best(
            @ApiParam(name = "team")
            @PathParam("team") String team) {

        Optional<Game> best = gp.byTeam(team)
                .filter(finished())
                .reduce((g1, g2) -> {

                    int g1Score;
                    if (g1.getHome().equals(team)) {
                        g1Score = Integer.parseInt(g1.getHomeScore())
                        - Integer.parseInt(g1.getVisitorScore());
                    } else {
                        g1Score = Integer.parseInt(g1.getVisitorScore())
                        - Integer.parseInt(g1.getHomeScore());;
                    }

                    int g2Score;
                    if (g2.getHome().equals(team)) {
                        g2Score = Integer.parseInt(g2.getHomeScore())
                        - Integer.parseInt(g2.getVisitorScore());
                    } else {
                        g2Score = Integer.parseInt(g2.getVisitorScore())
                        - Integer.parseInt(g2.getHomeScore());
                    }

                    if (g1Score > g2Score) {
//                        LOG.info(g1.getId() + " (" + g1Score + ") > ("
//                                + g2.getId() + " (" + g2Score + ")");
                        return g1;
                    } else {
//                        LOG.info(g2.getId() + " (" + g2Score + ") > ("
//                                + g1.getId() + " (" + g1Score + ")");
                        return g2;
                    }

                });

        if (best.isPresent()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(team + ": " + best.get());
            }
            return Response.ok(best.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

    }


    @GET
    @Timed
    @Path("{team}/{year}")
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


    private final static BinaryOperator<Game> better(String team) {

        final BinaryOperator<Game> operator = (g1, g2) -> {

            int g1Score = 0;
            if (g1.getHome().equals(team)) {
                g1Score = Integer.parseInt(g1.getHomeScore());
            } else {
                g1Score = Integer.parseInt(g1.getVisitorScore());
            }

            int g2Score = 0;
            if (g2.getHome().equals(team)) {
                g2Score = Integer.parseInt(g2.getHomeScore());
            } else {
                g2Score = Integer.parseInt(g2.getVisitorScore());
            }

            if (g1Score > g2Score) {
                return g1;
            } else {
                return g2;
            }

        };

        return operator;
    }


}
