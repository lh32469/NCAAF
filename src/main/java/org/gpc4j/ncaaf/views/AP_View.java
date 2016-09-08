package org.gpc4j.ncaaf.views;

import com.google.common.base.Strings;
import io.dropwizard.views.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.TeamProvider;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Path;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.jaxb.Week;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
public class AP_View extends View {

    /**
     * Default, not found image.
     */
    private static final String image = "http://www.marook-online.de/tp-images/"
            + "1uid106189-3d-glossy-orange-orb-icon-signs-no-walking1.png";

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


    public List<Path> getPaths() {

        final List<Path> paths = new ArrayList<>();

        // Previous week pointer.
        List<Team> previous = Collections.EMPTY_LIST;

        for (Week week : weeks) {
            List<Team> current = week.getTeams();

            for (Team team : current) {
                if (previous.contains(team)) {

                    int previousPosition = previous.indexOf(team) + 1;
                    int currentPosition = current.indexOf(team) + 1;
                    int volatility = week.getVolatility();
                    volatility
                            += Math.abs(currentPosition - previousPosition);
                    week.setVolatility(volatility);

                    Team lastWeek = previous.get(previous.indexOf(team));

                    int startX = lastWeek.getCX() + 70;
                    int startY = lastWeek.getCY() + 70 / 2;
                    int endX = team.getCX();
                    int endY = team.getCY() + 70 / 2;

                    String cpath = "M "
                            + startX + " "
                            + startY + " C "
                            + (startX + 50) + " "
                            + (startY) + " "
                            + (endX - 50) + " "
                            + (endY) + " "
                            + endX + " "
                            + endY;

                    Path p = new Path();
                    p.setD(cpath);

                    // See if last week was a Bye Week
                    Team lwOpp = getOpponent(week.getNumber() - 1, lastWeek);
                    String lastWeeksOpponentName = lwOpp.getName();

                    if (endY > startY) {
                        p.setStroke("red");
                    } else if (endY < startY) {
                        p.setStroke("green");
                    } else {
                        p.setStroke("blue");
                    }

                    if (lastWeeksOpponentName != null
                            && lastWeeksOpponentName.contains("Bye")) {
                        p.setStroke("black");
                    }

                    paths.add(p);

                }
            }

            previous = current;
        }
        return paths;
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
        LOG.debug("[" + team.getName() + "]");

        final String name = team.getName();
        final AtomicInteger wins = new AtomicInteger(0);
        final AtomicInteger losses = new AtomicInteger(0);

        gp.getGames().forEach((game) -> {
            if (game.getHomeScore() != null) {

                try {
                    if (game.getHome().equals(name)) {
                        int home = Integer.parseInt(game.getHomeScore());
                        int visitor = Integer.parseInt(game.getVisitorScore());
                        LOG.trace("Home: " + name);
                        if (home > visitor) {
                            LOG.debug("Win@Home: " + name);
                            wins.incrementAndGet();
                        } else {
                            LOG.debug("Loss@Home: " + name);
                            losses.incrementAndGet();
                        }
                    } else if (game.getVisitor().equals(name)) {
                        LOG.trace("Visitor: " + name);
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


    public Team getOpponent(int week, Team team) {

        Game game = getGame(week, team);

        final String home = game.getHome();
        final String visitor = game.getVisitor();

        if (home.equals(team.getName())) {
            LOG.debug("Found Home: " + visitor + " @ " + home);
            //LOG.info(game.toString());
            return tp.getTeam(visitor);
        } else if (visitor.equals(team.getName())) {
            LOG.debug("Found Vistor: " + visitor + " @ " + home);
            // LOG.info(game.toString());
            return tp.getTeam(home);
        } else {
            Team t = new Team();
            t.setName("Unknown");
            t.setImage(image);
            return t;
        }

    }


    public Game getGame(int week, Team team) {
        LOG.debug(week + ": " + team.getName());

        final String teamName = team.getName().trim();
        final List<Game> results
                = Collections.synchronizedList(new LinkedList<>());

        gp.getGames().forEachOrdered((game) -> {

            final String home = game.getHome();
            final String visitor = game.getVisitor();
            // LOG.info("[" + visitor + "] @ [" + home + "]");

            if (home.equals(teamName)) {
                LOG.debug("Found Home: " + visitor + " @ " + home);
                results.add(game);
            } else if (visitor.equals(teamName)) {
                LOG.debug("Found Vistor: " + visitor + " @ " + home);
                results.add(game);
            }

        });

        LOG.debug("Results Found: " + results.size());

        Game game;

        try {
            game = results.get(week);
        } catch (IndexOutOfBoundsException ex) {
            LOG.warn("No Game Week " + week + " for " + team.getName());
            LOG.warn("Results Found: " + results);
            game = new Game();
            game.setHome("Bye");
            game.setVisitor("Bye");
        }

//        LOG.info(week + ": " + team.getName()
//                + " vs " + results.get(0));
        return game;
    }


    /**
     * Format Game result for displaying as tooltip on game Icon.
     *
     * @param week Week number.
     * @param team One of the teams involved in the Game.
     * @return
     */
    public String getResult(int week, Team team) {

        StringBuilder sb = new StringBuilder();
        Game g = getGame(week, team);

        sb.append(g.getHome());

        if (!Strings.isNullOrEmpty(g.getHomeScore())) {
            sb.append(" (");
            sb.append(g.getHomeScore());
            sb.append(") / ");
        } else {
            sb.append(" / ");
        }

        sb.append(g.getVisitor());

        if (!Strings.isNullOrEmpty(g.getVisitorScore())) {
            sb.append(" (");
            sb.append(g.getVisitorScore());
            sb.append(")");
        }

        return sb.toString();
    }


}
