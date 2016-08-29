package org.gpc4j.ncaaf;

import org.gpc4j.ncaaf.resources.*;
import com.codahale.metrics.annotation.Timed;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Scanner;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import redis.clients.jedis.JedisPool;


/**
 *
 *
 *
 * @author ltharris
 */
@Path("update")
public class UpdateScheduleOld {

    //http://riccomini.name/posts/game-time-baby/2012-09-29-streaming-live-sports-schedule-scores-stats-api/
    static final String espn = "http://www.espn.com/ncf/bottomline/scores";

    static final DateTimeFormatter dtf
            = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a 'ET' yyyy");


    public static void main3(String[] args) {
        String text = "MON, SEP 5 8:00 PM ET";
        text = "MON, Sep 5 8:00 PM ET";
        // text = "Fri, Aug 26 12:41 PM";

//        DateTimeFormatter formatter
//                = DateTimeFormatter.ofPattern("EEE, MMM dd KK:mm a z");
//        formatter
//                = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a 'ET' yyyy");
        System.out.println("Date: " + LocalDateTime.now().format(dtf));
        System.out.println("Date: " + text);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        System.out.println("Year: " + year);

        LocalDateTime dateTime = LocalDateTime.parse(text + " " + year, dtf);
        System.out.println("Date: " + dateTime);

        System.out.println("Parse: " + LocalDateTime.parse(dateTime.toString()));
    }


    public static void main4(String[] args) {
        getDate("1=Hawaii at California (FRI, AUG 26 10:00 PM ET)");
    }


    @Inject
    private JedisPool pool;


    @GET
    @Timed
    public Response update() throws Exception {
        UpdateScheduleOld.main(null);
        return Response.ok().build();
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        URL url = new URL(espn);

        String out = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
        String text = URLDecoder.decode(out, "UTF-8");
        System.out.println(text);

        Scanner scan = new Scanner(text).useDelimiter("ncf_s_left");

        while (scan.hasNext()) {

            String entry = scan.next();
            System.out.println(entry);

            Scanner s1 = new Scanner(entry).useDelimiter("&ncf");
            String teamsAndDate = s1.next();
            System.out.println("   " + teamsAndDate);
            System.out.println("      Id: " + getGameId(entry));

            Scanner s2 = new Scanner(teamsAndDate).useDelimiter(" at ");

            String visitorRaw = s2.next();
            String visitor = getVisitor(visitorRaw);
            String visitorRank = getVisitorRank(visitorRaw);
            System.out.println("      V: " + visitor);
            System.out.println("      VR: " + visitorRank);

            if (s2.hasNext()) {
                String homeRaw = s2.next();
                System.out.println("      H:  " + getHomeTeam(homeRaw));
                System.out.println("      HR: " + getHomeRank(homeRaw));
                System.out.println("      D:  " + getDate(homeRaw));
            }
        }

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
    static String getVisitor(String text) {
        // System.out.println("      gv: " + text);
        String p2 = text.split("=")[1];

        if (p2.startsWith("(")) {
            // Ranked team
            return p2.split("\\) ")[1];
        } else {
            return p2;
        }

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
     * Get home team from text provided which also includes date.
     *
     * Examples:
     *
     * Texas (SUN, SEP 4 7:30 PM ET) <br/>
     * (1) Alabama (SAT, SEP 3 8:00 PM ET)
     *
     * @param text
     * @return
     */
    static String getHomeTeam(String text) {

        if (text.startsWith("(")) {
            // Ranked team
            text = text.split("\\) ")[1];
        }

        // Remove trailing date
        return text.split("\\(")[0];
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
    static String getGameId(String text) {

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


}
