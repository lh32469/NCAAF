package org.gpc4j.ncaaf;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import static org.hamcrest.core.Is.is;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 *
 * @author Lyle T Harris
 */
public class BowlGamesTest {

    private static JedisPool pool;

    private static GamesProvider gp;

    private static TeamProvider tp;

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(BowlGamesTest.class);


    @BeforeClass
    public static void setUpClass() {
        JedisPoolConfig cfg = new JedisPoolConfig();

        //  pool = new JedisPool(cfg, "localhost", 6379, 0, "welcome1", 10, "JUnit");
        pool = new JedisPool(cfg, "macmini.local", 6388, 0, "welcome1", 10, "JUnit");

        gp = new GamesProvider(pool);
        tp = new TeamProvider(pool);
    }




    @Test
    public void orangeBowl2017() {
        Team team = new Team();
        team.setName("Wisconsin");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Miami"));
        assertThat(game.getVisitor(), is("Wisconsin"));
    }




}
