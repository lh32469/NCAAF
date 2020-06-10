package org.gpc4j.ncaaf.views;

import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.TeamProvider;
import org.gpc4j.ncaaf.hystrix.GetTeamCommand;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.redis.RedisGamesProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 *
 * @author ltharris
 */
public class AP_ViewTest {

    private static JedisPool pool;

    private static GamesProvider gp;

    private static TeamProvider tp;

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(AP_ViewTest.class);


    @BeforeClass
    public static void setUpClass() {
        JedisPoolConfig cfg = new JedisPoolConfig();

        pool = new JedisPool(cfg, "macmini.local", 6388, 0, "welcome1", 10, "JUnit");
        gp = new RedisGamesProvider(pool);
        tp = new TeamProvider(pool);
    }


    @AfterClass
    public static void tearDownClass() {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    @Test
    public void LSU_Week1() {

        final String teamName = "LSU";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(0, team);
        assertEquals("Wisconsin", opponent.getName());
    }


    @Test
    public void Wisconsin_Week1() {

        final String teamName = "Wisconsin";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(0, team);
        assertEquals("LSU", opponent.getName());
    }


    @Test
    public void Wisconsin_Week2() {

        final String teamName = "Wisconsin";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(1, team);
        assertEquals("Akron", opponent.getName());
    }


    @Test
    public void Michigan_Week2() {

        final String teamName = "Michigan";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(1, team);
        assertEquals("UCF", opponent.getName());
    }


    @Test
    public void Alabama_Week2() {

        final String teamName = "Alabama";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(1, team);
        assertEquals("Western Kentucky", opponent.getName());

//        Game game = instance.getGame(1, team);
//        System.out.println("" + game);
    }


    //  // @Test
    public void getRecordFloridaState() {

        final String teamName = "Florida State";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        String record = instance.getRecord(1, team);
        assertNotNull(record);
        LOG.info(record);
        assertEquals("2 - 1", record);

    }


    @Test
    public void MichiganState_Week1() {

        final String teamName = "Michigan State";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(0, team);
        assertEquals("Furman", opponent.getName());

//        Game game = instance.getGame(0, team);
//        System.out.println("" + game);
    }


    @Test
    public void MichiganState_Week4() {

        final String teamName = "Michigan State";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(3, team);
        assertEquals("Wisconsin", opponent.getName());

//        Game game = instance.getGame(3, team);
//        System.out.println("" + game);
    }


  //  @Test
    public void Mississippi_Week4() {

        final String teamName = "Mississippi";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(3, team);
        assertEquals("Georgia", opponent.getName());

//        Game game = instance.getGame(3, team);
//        System.out.println("" + game);
    }


   // @Test
    public void Miami_Florida() {

        final String teamName = "Miami (FL)";
        Jedis j = pool.getResource();
        Team team = new GetTeamCommand(teamName, j).execute();
        pool.returnResource(j);
        assertEquals(teamName, team.getName());

        AP_View instance = new AP_View(2016);
        instance.setGp(gp);
        instance.setTp(tp);

        Team opponent = instance.getOpponent(5, team);
        assertEquals("Florida State", opponent.getName());

    }


}
