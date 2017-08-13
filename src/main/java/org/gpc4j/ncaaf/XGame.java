package org.gpc4j.ncaaf;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import org.gpc4j.ncaaf.jaxb.Game;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;


/**
 *
 * @author Lyle T Harris
 */
public class XGame extends Game {

    static final private Pattern FINAL_SCORES
            = Pattern.compile("(\\D+) (\\d+)");

    static final private DateTimeFormatter dtf
            = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a 'ET' yyyy");

    /**
     * Initial entry string from ESPN.
     */
    private String entry;

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(XGame.class);

    private int year;


    public XGame(Game g) {
        setHome(g.getHome());
        setHomeRank(g.getHomeRank());
        setHomeScore(g.getHomeScore());
        setVisitor(g.getVisitor());
        setVisitorRank(g.getVisitorRank());
        setVisitorScore(g.getVisitorScore());
        setDate(g.getDate());
        setId(g.getId());
        setKey(g.getKey());
    }


    public XGame(String entry) {
        this(entry,  Calendar.getInstance().get(Calendar.YEAR));
    }


    public XGame(String entry, int year) {
        this.entry = entry;
        this.year = year;
        setId(getGameId(entry));

        Scanner s1 = new Scanner(entry).useDelimiter("&ncf");
        String teamsAndDate = s1.next();
        LOG.debug("   " + teamsAndDate);

        if (teamsAndDate.contains("FINAL")) {
            teamsAndDate = teamsAndDate.replaceAll("\\^", "");
            Scanner s2 = new Scanner(teamsAndDate).useDelimiter("   ");
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
        setHomeRank(data.get("homeRank"));
        setHomeScore(data.get("homeScore"));
        setVisitor(data.get("visitor"));
        setVisitorRank(data.get("visitorRank"));
        setVisitorScore(data.get("visitorScore"));
        setDate(data.get("date"));
    }


    public void saveGame(Jedis jedis) {
        final String key = "game." + year + "." + getId();
        safeSet(jedis, key, "home", getHome());
        safeSet(jedis, key, "homeRank", getHomeRank());
        safeSet(jedis, key, "homeScore", getHomeScore());
        safeSet(jedis, key, "visitor", getVisitor());
        safeSet(jedis, key, "visitorRank", getVisitorRank());
        safeSet(jedis, key, "visitorScore", getVisitorScore());
        safeSet(jedis, key, "date", getDate());
    }


    /**
     * Sets the field in the key only if the field is not null or empty.
     */
    void safeSet(Jedis jedis, String key, String field, String val) {
        if (!Strings.isNullOrEmpty(val)) {
            jedis.hset(key, field, val);
        }
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

        Matcher m = FINAL_SCORES.matcher(p2);
        if (m.find()) {
            String found = m.group(1);
            p2 = found;
            LOG.debug("Found: " + found + ", score: ", m.group(2));
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

        // Miami (OH) (SAT, SEP 10 3:30 PM ET)
        Pattern d
                = Pattern.compile("(.*)\\((MON.*|TUE.*|WED.*|THU.*|FRI.*|SAT.*|SUN.*|FINAL.*)\\)");
        Matcher m = d.matcher(p2);

        if (m.find()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("** Found: " + m.groupCount());
                for (int i = 1; i <= m.groupCount(); i++) {
                    LOG.debug(" >>  " + m.group(i));
                }
            }
            p2 = m.group(1).trim();
        }

        LOG.debug("Searching: " + p2);

        m = FINAL_SCORES.matcher(p2);
        if (m.find()) {
            String found = m.group(1);
            p2 = found;
            LOG.debug("Found: " + found + ", score: ", m.group(2));
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
     * (1) Alabama (SAT, SEP 3 8:00 PM ET)<br/>
     * Miami (OH) (SAT, SEP 10 3:30 PM ET)
     *
     * @param text
     * @return
     */
    String getDate(String text) {

        LOG.debug(text);

        if (text.startsWith("(")) {
            // Ranked team
            text = text.split("\\) ")[1];
        }

        Pattern d
                = Pattern.compile(".*(MON.*|TUE.*|WED.*|THU.*|FRI.*|SAT.*|SUN.*)\\)");
        Matcher m = d.matcher(text);

        if (m.find()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("** Found: " + m.groupCount());
                for (int i = 1; i <= m.groupCount(); i++) {
                    LOG.debug(" >>  " + m.group(i));
                }
            }
            text = m.group(1).trim();
        }

        LOG.debug(text);

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
        text = text.replaceAll("NOV", "Nov");
        text = text.replaceAll("DEC", "Dec");

        text += " " + year;

        String result = null;
        try {
            LOG.debug("Parsing: [" + text + "]");
            result = LocalDateTime.parse(text, dtf).toString();
        } catch (DateTimeParseException ex) {
            LOG.error(ex.getLocalizedMessage());
            LOG.error(entry);
        }

        return result;
    }


    @Override
    public String toString() {
        return "Game{" + "visitor=" + visitor + ", visitorRank=" + visitorRank
                + ", visitorScore=" + visitorScore + ", home=" + home
                + ", homeRank=" + homeRank + ", homeScore=" + homeScore
                + ", date=" + date + ", id=" + id + '}';
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XGame other = (XGame) obj;
        return other.id.equals(id);
    }


}
