package org.gpc4j.ncaaf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gpc4j.ncaaf.jaxb.Game;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
public class XGame extends Game {

    static final DateTimeFormatter dtf
            = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a 'ET' yyyy");

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(XGame.class);


    public XGame(String entry) {
        setId(getGameId(entry));

        Scanner s1 = new Scanner(entry).useDelimiter("&ncf");
        String teamsAndDate = s1.next();
        LOG.info("   " + teamsAndDate);

        if (teamsAndDate.contains("FINAL")) {

            Scanner s2 = new Scanner(teamsAndDate).useDelimiter("\\^");
            String visitorRaw = s2.next();
            setVisitor(parseVistor(visitorRaw));
            setVisitorRank(getVisitorRank(visitorRaw));

            if (s2.hasNext()) {
                String homeRaw = s2.next();
                setHome(parseHomeTeam(homeRaw));
                setHomeRank(getHomeRank(homeRaw));
                // setDate(getDate(homeRaw));
            }

        } else {

            Scanner s2 = new Scanner(teamsAndDate).useDelimiter(" at ");

            String visitorRaw = s2.next();
            setVisitor(parseVistor(visitorRaw));
            setVisitorRank(getVisitorRank(visitorRaw));

            if (s2.hasNext()) {
                String homeRaw = s2.next();
                setHome(parseHomeTeam(homeRaw));
                setHomeRank(getHomeRank(homeRaw));
                setDate(getDate(homeRaw));
            }
        }

    }


    /**
     * Construct from Redis data.
     *
     * @param data
     */
    public XGame(Map<String, String> data) {
        setHome(data.get("home"));
        setVisitor(data.get("visitor"));
        setHomeRank(data.get("homeRank"));
        setHomeScore(data.get("homeScore"));
    }


    /**
     *
     * Example:
     *
     * 86=(9) Notre Dame at Texas (SUN, SEP 4 7:30 PM
     * ET)&ncf_s_right86_count=0&ncf_s_url86=http://espn.go.com/ncf/preview?gameId=400868946&
     *
     * 87=(12) Ole Miss at (4) Florida State (MON, SEP 5 8:00 PM
     * ET)&ncf_s_right87_count=0&ncf_s_url87=http://espn.go.com/ncf/preview?gameId=400868979&ncf_s_loaded=true
     *
     * @param text
     * @return
     */
    final String getGameId(String text) {

        String id = "unknown";

        Scanner s1 = new Scanner(text).useDelimiter("gameId=");
        // Skip everything before delimiter
        String next = s1.next();
        // System.out.println("     s1: " + next);
        if (s1.hasNext()) {
            next = s1.next();
            //  System.out.println("       s1: " + next);
            Scanner s2 = new Scanner(next).useDelimiter("&");
            id = s2.next();
            //  System.out.println("         s2: " + id);
        }

        return id;
    }


    /**
     * Get visiting team from text provided.
     *
     * Examples:
     *
     * 82=(2) Clemson <br/>
     * 85=Northern Arizona
     *
     * @param text
     * @return
     */
    String parseVistor(String text) {
        LOG.debug(text);
        text = text.trim();
        String p2 = text.split("=")[1];

        if (p2.startsWith("(")) {
            // Ranked team, get everthing after ranking.
            p2 = p2.split("\\) ")[1];
        }

        LOG.trace("Searching: " + p2);
        Pattern p = Pattern.compile("([\\w\\s]+) (\\d+)");

        Matcher m = p.matcher(p2);
        if (m.find()) {
            String found = m.group(1);
            LOG.trace("debug: " + found);
            p2 = found;
            LOG.debug("Score: " + m.group(2));
            setVisitorScore(m.group(2));
        }

        return p2;

    }


    /**
     * Get home team from text provided which also includes date.
     *
     * Examples:
     *
     * Texas (SUN, SEP 4 7:30 PM ET) <br/>
     * (1) Alabama (SAT, SEP 3 8:00 PM ET) <br/>
     * California 51 (FINAL)
     *
     * @param p2
     * @return
     */
    String parseHomeTeam(String p2) {
        LOG.debug(p2);

        if (p2.startsWith("(")) {
            // Ranked team, get everthing after ranking.
            p2 = p2.split("\\) ")[1];
        }

        // Get everything before parens
        p2 = p2.split("\\(")[0].trim();
        LOG.trace("Searching: " + p2);
        Pattern p = Pattern.compile("([\\w\\s]+) (\\d+)");

        Matcher m = p.matcher(p2);
        if (m.find()) {
            String found = m.group(1);
            LOG.debug("Found: " + found);
            p2 = found;
            setHomeScore(m.group(2));
        }

        return p2;
    }


    /**
     * Get visiting team rank from text provided or empty String if not ranked.
     *
     * Examples:
     *
     * 82=(2) Clemson <br/>
     * 85=Northern Arizona
     *
     * @param text
     * @return
     */
    static String getVisitorRank(String text) {

        String p2 = text.split("=")[1];

        if (p2.startsWith("(")) {
            // Ranked team
            p2 = p2.replaceAll("\\(", "");
            p2 = p2.replaceAll("\\)", "");

            return p2.split(" ")[0];
        } else {
            return "";
        }

    }


    /**
     * Get home team rank from text provided or empty String if not ranked.
     *
     * Examples:
     *
     * Texas (SUN, SEP 4 7:30 PM ET) <br/>
     * (1) Alabama (SAT, SEP 3 8:00 PM ET)
     *
     * @param text
     * @return
     */
    static String getHomeRank(String text) {
        LOG.trace(text);
        if (text.startsWith("(")) {
            // Ranked team
            text = text.replaceAll("\\(", "");
            text = text.replaceAll("\\)", "");

            return text.split(" ")[0];
        } else {
            return "";
        }

    }


    /**
     * Get date from text provided which also includes home team.
     *
     * Examples:
     *
     * Texas (SUN, SEP 4 7:30 PM ET) <br/>
     * (1) Alabama (SAT, SEP 3 8:00 PM ET)
     *
     * @param text
     * @return
     */
    static String getDate(String text) {

        if (text.startsWith("(")) {
            // Ranked team
            text = text.split("\\) ")[1];
        }

        text
                = text.split("\\(")[1].replaceAll("\\)", "");

        // Replace days of week
        text = text.replaceAll("MON", "Mon");
        text = text.replaceAll("TUE", "Tue");
        text = text.replaceAll("WED", "Wed");
        text = text.replaceAll("THU", "Thu");
        text = text.replaceAll("FRI", "Fri");
        text = text.replaceAll("SAT", "Sat");
        text = text.replaceAll("SUN", "Sun");

        // Replace months
        text = text.replaceAll("AUG", "Aug");
        text = text.replaceAll("SEP", "Sep");
        text = text.replaceAll("OCT", "Oct");

        text += " " + Calendar.getInstance().get(Calendar.YEAR);

        return LocalDateTime.parse(text, dtf).toString();
    }


    @Override
    public String toString() {
        return "Game{" + "visitor=" + visitor + ", visitorRank=" + visitorRank
                + ", visitorScore=" + visitorScore + ", home=" + home
                + ", homeRank=" + homeRank + ", homeScore=" + homeScore
                + ", date=" + date + ", id=" + id + '}';
    }


}
