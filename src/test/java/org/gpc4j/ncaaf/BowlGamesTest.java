package org.gpc4j.ncaaf;

import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.gpc4j.ncaaf.redis.RedisGamesProvider;
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

        gp = new RedisGamesProvider(pool);
        tp = new TeamProvider(pool);
    }


    @Test
    public void orangeBowlWisconsin_2017() {
        Team team = new Team();
        team.setName("Wisconsin");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Miami"));
        assertThat(game.getVisitor(), is("Wisconsin"));
    }


    @Test
    public void orangeBowlMiami_2017() {
        Team team = new Team();
        team.setName("Miami");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Miami"));
        assertThat(game.getVisitor(), is("Wisconsin"));
    }


    @Test
    public void roseBowlGeorgia_2018() {
        Team team = new Team();
        team.setName("Georgia");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Oklahoma"));
        assertThat(game.getVisitor(), is("Georgia"));
    }


    @Test
    public void roseBowlOklahoma_2018() {
        Team team = new Team();
        team.setName("Oklahoma");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Oklahoma"));
        assertThat(game.getVisitor(), is("Georgia"));
    }


    @Test
    public void sugarBowlClemson_2018() {
        Team team = new Team();
        team.setName("Clemson");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Clemson"));
        assertThat(game.getVisitor(), is("Alabama"));
    }


    @Test
    public void sugarBowlAlabama_2018() {
        Team team = new Team();
        team.setName("Alabama");
        Game game = gp.getGame(team, 2017, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Clemson"));
        assertThat(game.getVisitor(), is("Alabama"));
    }


}
