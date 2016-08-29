package org.gpc4j.ncaaf.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.gpc4j.ncaaf.XGame;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Schedule;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 *
 *
 * @author ltharris
 */
@Path("update")
public class UpdateSchedule {

    //http://riccomini.name/posts/game-time-baby/2012-09-29-streaming-live-sports-schedule-scores-stats-api/
    static final String espn = "http://www.espn.com/ncf/bottomline/scores";

    static final DateTimeFormatter dtf
            = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a 'ET' yyyy");

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(UpdateSchedule.class);

    private Jedis jedis;

    @Inject
    private JedisPool pool;


    @PostConstruct
    public void postConstruct() {
        jedis = pool.getResource();
        LOG.debug("Jedis: " + jedis);
    }


    @PreDestroy
    public void preDestroy() {
        LOG.debug("Jedis: " + jedis);
        pool.returnResource(jedis);
    }


    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Schedule update() throws Exception {
        return doUpdate();
    }


    Schedule doUpdate() throws Exception {

        Schedule sched = new Schedule();

        URL url = new URL(espn);

        InputStream iStream = url.openStream();
        String out = new Scanner(iStream, "UTF-8").useDelimiter("\\A").next();
        String text = URLDecoder.decode(out, "UTF-8");

        Scanner scan = new Scanner(text).useDelimiter("ncf_s_left");

        while (scan.hasNext()) {
            Game game = new XGame(scan.next());
            if (!Strings.isNullOrEmpty(game.getHome())) {
                sched.getGames().add(game);
            }
        }

        for (Game game : sched.getGames()) {
            //System.out.println(game.toString());
            final String id = "game.2016." + game.getId();
            safeSet(id, "home", game.getHome());
            safeSet(id, "homeRank", game.getHomeRank());
            safeSet(id, "homeScore", game.getHomeScore());
            safeSet(id, "visitor", game.getVisitor());
            safeSet(id, "visitorRank", game.getVisitorRank());
            safeSet(id, "visitorScore", game.getVisitorScore());
            safeSet(id, "date", game.getDate());
            //jedis.expire(id, 3600);
            //jedis.persist(id);
        }

        return sched;
    }


    void safeSet(String key, String hkey, String val) {
        if (!Strings.isNullOrEmpty(val)) {
            jedis.hset(key, hkey, val);
        }
    }


}
