package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import org.gpc4j.ncaaf.PollProvider;
import org.gpc4j.ncaaf.XTeam;
import org.gpc4j.ncaaf.jaxb.Week;
import org.gpc4j.ncaaf.providers.GamesProvider;
import org.gpc4j.ncaaf.providers.TeamProvider;
import org.gpc4j.ncaaf.ravendb.dto.Poll;
import org.gpc4j.ncaaf.views.AP_View;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * AP_View Top 25.
 *
 * @author Lyle T Harris
 */
@Path("{parameter: ap|AP}")
public class AP {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP.class);

    @Inject
    private PollProvider rp;

    @Inject
    private GamesProvider gp;

    @Inject
    private TeamProvider tp;

    @Inject
    AP_View view;


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


    public Stream<Week> getWeeks(int year)  {
        LOG.debug(year + "");
        final LinkedList<Week> weeks = new LinkedList<>();

        List<Poll> polls = rp.getPolls(year)
            .collect(Collectors.toList());

        int xPosition = 0;

        for (Poll poll : polls) {

            Week week = new Week();
            week.setNumber(poll.getWeek());
            xPosition += 250;
            week.setXPos(xPosition);

            int y = 25;

            for (String teamId : poll.getTeams()) {
                XTeam team = (XTeam) tp.getTeam(teamId.trim());
                team.setCX(week.getXPos());
                team.setCY(y += 75);
                week.getTeams().add(team);
            }

            weeks.add(week);
        }

        return weeks.parallelStream();
    }


}
