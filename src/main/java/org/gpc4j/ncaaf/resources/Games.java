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
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
@Path("games")
public class Games {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP.class);

    @Inject
    private GamesProvider gp;


    @GET
    @Timed
    @Path("{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Game> getGames(@PathParam("year") Integer year) {
        LOG.info(year.toString());
        return gp.getGames()
                .filter((game) -> !Strings.isNullOrEmpty(game.getDate())
                        && game.getDate().contains(year.toString()))
                .collect(Collectors.toList());
    }


}
