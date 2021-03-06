package org.gpc4j.ncaaf.views;

import com.google.common.base.Strings;
import io.dropwizard.views.View;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.TeamProvider;
import org.gpc4j.ncaaf.XTeam;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Path;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.jaxb.Week;
import org.gpc4j.ncaaf.resources.GamesResource;
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

    private static final String BYE
            = "http://www.gpc4j.org/ncaaf/images/bye.png";

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP_View.class);

    private List<Week> weeks;

    private String title;

    private TeamProvider tp;

    private GamesProvider gp;

    private final int year;

    private final static Game BYE_GAME;

    private final List<LocalDateTime> saturdays = new LinkedList<>();

    static final List<Game> badGames = new LinkedList<>();

    private static final ZoneId EST = ZoneId.of("US/Eastern");

    private static final ZoneId PST = ZoneId.of("US/Pacific");

    private static final DateTimeFormatter DTF
            = DateTimeFormatter.ofPattern("MMMM dd, hh:mm a z");


    static {
        BYE_GAME = new Game();
        BYE_GAME.setHome("Bye");
        BYE_GAME.setVisitor("BYE");
    }


    public AP_View(int year) {
        super("ap.ftl");
        this.year = year;

        LocalDateTime date = LocalDateTime.parse(year + "-09-03T20:00");
        saturdays.add(date);

        for (int i = 0; i < 20; i++) {
            date = date.plusDays(7);
            saturdays.add(date);
            LOG.debug("Week: " + i + " = " + date);
        }
    }


    public void setTp(TeamProvider tp) {
        this.tp = tp;
    }


    public void setGp(GamesProvider gp) {
        this.gp = gp;
    }


    public List<Week> getWeeks() {
        LOG.info(weeks.size() + "");
        if (LOG.isTraceEnabled()) {
            gp.getGames().forEach(g -> {
                if (g.getDate() == null) {
                    LOG.trace("No date set: " + g.getKey() + ", "
                            + g.getHome() + " vs " + g.getVisitor());
                }
            });
        }
        return weeks;
    }


    public List<Path> getPaths() {
        LOG.info(weeks.size() + "");

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

//                    // See if last week was a Bye Week
//                    Team lwOpp = getOpponent(week.getNumber() - 1, lastWeek);
//                    String lastWeeksOpponentName = lwOpp.getName();
//
//                    if (endY > startY) {
//                        p.setStroke("red");
//                    } else if (endY < startY) {
//                        p.setStroke("green");
//                    } else {
//                        p.setStroke("blue");
//                    }
//
//                    if (lastWeeksOpponentName != null
//                            && lastWeeksOpponentName.contains("Bye")) {
//                        p.setStroke("black");
//                    }
                    paths.add(p);
                    // Default stroke-width
                    p.setStrokeWidth("1");
                    p.setStroke("black");

                    if (week.getNumber() == 0) {
                        continue;
                    }

                    // Get prior week's game to see how big a margin
                    // it was won/lost by.
                    Optional<Game> opt
                            = gp.getGame(team, year, week.getNumber() - 1);

                    if (!opt.isPresent()) {
                        continue;
                    }

                    Game g = opt.get();

                    // Determine if Win or Loss
                    boolean win = false;

                    if (g.getHomeScore() != null) {
                        int homeScore = Integer.parseInt(g.getHomeScore());
                        int visitorScore = Integer.parseInt(g.getVisitorScore());

                        // See if team is home or visitor
                        if (team.getName().equals(g.getHome())) {
                            // Team at Home
                            if (homeScore > visitorScore) {
                                win = true;
                            }
                        } else {
                            // Team at Visitor
                            if (visitorScore > homeScore) {
                                win = true;
                            }
                        }

                        if (win) {
                            p.setStroke("green");
                        } else {
                            p.setStroke("red");
                        }

                        int diff = Math.abs(homeScore - visitorScore);

                        if (diff > 40) {
                            p.setStrokeWidth("4");
                        } else if (diff > 20) {
                            p.setStrokeWidth("3");
                        } else if (diff > 10) {
                            p.setStrokeWidth("2");
                        }
                    }

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


    public String getRecord(int week, Team team) {

        final String teamName = team.getName().trim();

        LocalDateTime gDay = saturdays.get(week);
        LOG.debug(week + ": " + teamName + ", GameDay: " + gDay);

        List<Game> games = gp.gamesPlayed(teamName, year)
                .filter(g -> {
                    LocalDateTime gDate = LocalDateTime.parse(g.getDate());
                    return gDate.isBefore(gDay.minusDays(3));
                })
                .collect(Collectors.toList());

        long wins = games
                .stream()
                .filter(GamesResource.win(teamName))
                .count();

        return wins + " - " + (games.size() - wins);
    }


    public Team getOpponent(int week, Team team) {

        Optional<Game> opt = gp.getGame(team, year, week);

        if (opt.isPresent()) {
            Game game = opt.get();

            final String home = game.getHome();
            final String visitor = game.getVisitor();

            if (home.equals(team.getName())) {
                LOG.debug("Found Home: " + visitor + " @ " + home);
                //LOG.info(game.toString());
                return tp.getTeam(visitor);
            } else {
                LOG.debug("Found Vistor: " + visitor + " @ " + home);
                // LOG.info(game.toString());
                return tp.getTeam(home);
            }
        } else {
            Team t = new XTeam();
            t.setName("Unknown");
            t.setImage(BYE);
            return t;
        }

    }


    /**
     * Get the rank of the Team provided in the List of Teams provided.
     *
     * @return Rank (position) in the List of Teams or -1 if not present.
     */
    public int getRank(final Team team, final List<Team> teams) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(team.getName() + " -> " + teams.indexOf(team));
        }
        return teams.indexOf(team);
    }


    /**
     * See if new games have been loaded in to DB for current week.
     *
     * @param week
     * @return
     */
    public boolean newGamesPosted(int week) {

        LocalDateTime gDay = saturdays.get(week);

        long games = gp.byYear(year)
                .filter(g -> {
                    LocalDateTime gDate = LocalDateTime.parse(g.getDate());
                    return gDay.plusDays(4).isAfter(gDate)
                            && gDay.minusDays(4).isBefore(gDate);
                }).count();

        return games > 10;
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
        Game g = gp.getGame(team, year, week).orElse(BYE_GAME);

        sb.append("<div>");
        sb.append(g.getVisitor());

        if (!Strings.isNullOrEmpty(g.getVisitorScore())) {
            sb.append(" (");
            sb.append(g.getVisitorScore());
            sb.append(")");
        }

        sb.append(" @ ");

        sb.append(g.getHome());

        if (!Strings.isNullOrEmpty(g.getHomeScore())) {
            sb.append(" (");
            sb.append(g.getHomeScore());
            sb.append(")");
        }

        if (g.getDate() != null) {
            ZonedDateTime gameTime
                    = LocalDateTime.parse(g.getDate()).atZone(EST);
            sb.append("<br/>");
            sb.append(gameTime.withZoneSameInstant(PST).format(DTF));
        }
        sb.append("</div>");

        return sb.toString();
    }


    /**
     * Format Game result for displaying as alert on game Icon.
     *
     * @param week Week number.
     * @param team One of the teams involved in the Game.
     * @return
     */
    public String getResultAsText(int week, Team team) {

        String result = getResult(week, team);

        result = result.replaceAll("<div>", "");
        result = result.replaceAll("</div>", "");
        result = result.replaceAll("<br/>", "\\\\n");

        return result;
    }


}
