package org.gpc4j.ncaaf.views;

import io.dropwizard.views.View;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.TeamProvider;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.jaxb.Week;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
public class AP_View extends View {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP_View.class);

    private List<Week> weeks;

    private String title;

    private TeamProvider tp;

    private GamesProvider gp;


    public AP_View() {
        super("ap.ftl");
    }


    public void setTp(TeamProvider tp) {
        this.tp = tp;
    }


    public void setGp(GamesProvider gp) {
        this.gp = gp;
    }


    public List<Week> getWeeks() {
        LOG.debug(weeks.size() + "");
        return weeks;
    }


    public void setWeeks(List<Week> weeks) {
        this.weeks = weeks;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getRecord(Team team) {

        final String name = team.getName();
        final AtomicInteger wins = new AtomicInteger(0);
        final AtomicInteger losses = new AtomicInteger(0);

        gp.getGames().forEach((game) -> {
            if (game.getHomeScore() != null) {

                try {
                    if (game.getHome().equals(name)) {
                        int home = Integer.parseInt(game.getHomeScore());
                        int visitor = Integer.parseInt(game.getVisitorScore());

                        if (home > visitor) {
                            LOG.debug("Win@Home: " + name);
                            wins.incrementAndGet();
                        } else {
                            LOG.debug("Loss@Home: " + name);
                            losses.incrementAndGet();
                        }
                    } else if (game.getVisitor().equals(name)) {
                        int home = Integer.parseInt(game.getHomeScore());
                        int visitor = Integer.parseInt(game.getVisitorScore());
                        if (visitor > home) {
                            LOG.debug("Win@Away: " + name);
                            wins.incrementAndGet();
                        } else {
                            LOG.debug("Loss@Away: " + name);
                            losses.incrementAndGet();
                        }
                    }
                } catch (NumberFormatException ex) {
                    LOG.error(ex.getLocalizedMessage() + " " + game);
                }

            }

        });

        return wins + " - " + losses;
    }


}
