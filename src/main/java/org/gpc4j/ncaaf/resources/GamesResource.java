package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
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
            = LoggerFactory.getLogger(AP.class);

    @Inject
    private GamesProvider gp;


    @GET
    @Timed
    @Path("{year}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getGames(@PathParam("year") Integer year) {
        LOG.info(year.toString());

        List<Game> games = gp.getGames()
                .filter((game) -> !Strings.isNullOrEmpty(game.getDate())
                        && game.getDate().contains(year.toString()))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        return g;
    }


    @GET
    @Timed
    @Path("{year}/{team}")
    @Produces({MediaType.APPLICATION_JSON + ";qs=1",
        MediaType.APPLICATION_XML + ";qs=0.5"})
    public Games getGamesForTeam(@PathParam("year") Integer year,
            @PathParam("team") String team) {
        LOG.info(year.toString() + "/" + team);

        List<Game> games = gp.getGames()
                .filter((game) -> !Strings.isNullOrEmpty(game.getDate())
                        && game.getDate().contains(year.toString()))
                .filter((game) -> game.getHome().equals(team)
                        || game.getVisitor().equals(team))
                .collect(Collectors.toList());

        Games g = new Games();
        g.getGame().addAll(games);

        return g;
    }


}
