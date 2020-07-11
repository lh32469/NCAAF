package org.gpc4j.ncaaf;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.core.Is.is;

import org.gpc4j.ncaaf.redis.RedisGamesProvider;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 *
 * @author Lyle T Harris
 */
public class GamesProviderTest {

    private static GamesProvider gp;

    private static TeamProvider tp;

    @ClassRule
    public static final DropwizardAppRule<FootballConfiguration> DROPWIZARD_APP_RULE =
        new DropwizardAppRule<>(
            FootballApplication.class,
            resourceFilePath("test-config.yml"));

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GamesProviderTest.class);


    @BeforeClass
    public static void setUpClass() {
        JedisPoolConfig cfg = new JedisPoolConfig();

        FootballConfiguration config = DROPWIZARD_APP_RULE.getConfiguration();

        JedisPool pool = new JedisPool(cfg,
            config.getRedisHost(),
            config.getRedisPort(),
            0,
            config.getRedisPass(),
            10,
            "JUnit");
        gp = new RedisGamesProvider(pool);
        tp = new TeamProvider(pool);
    }


    // @Test
    public void stanfordAtRice() {
        Team team = new Team();
        team.setName("Stanford");
        Game game = gp.getGame(team, 2017, 0).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Rice"));
        assertThat(game.getVisitor(), is("Stanford"));
    }


    @Test
    public void stanfordAtUSC() {
        Team team = new Team();
        team.setName("Stanford");
        Game game = gp.getGame(team, 2017, 1).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("USC"));
        assertThat(game.getVisitor(), is("Stanford"));
    }


    //  @Test
    public void michiganAtFlorida() {
        Team team = new Team();
        team.setName("Michigan");
        Game game = gp.getGame(team, 2017, 0).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Florida"));
        assertThat(game.getVisitor(), is("Michigan"));

        team.setName("Florida");
        game = gp.getGame(team, 2017, 0).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Florida"));
        assertThat(game.getVisitor(), is("Michigan"));
    }


    @Test
    public void cincinnatiAtMichigan() {
        Team team = new Team();
        team.setName("Michigan");
        Game game = gp.getGame(team, 2017, 1).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Michigan"));
        assertThat(game.getVisitor(), is("Cincinnati"));

        team.setName("Cincinnati");
        game = gp.getGame(team, 2017, 1).get();
        LOG.info(game.toString());
        assertThat(game.getVisitor(), is("Cincinnati"));
        assertThat(game.getHome(), is("Michigan"));
    }


    @Test
    public void AirForceAtMichigan() {
        Team team = new Team();
        team.setName("Michigan");
        Game game = gp.getGame(team, 2017, 2).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Michigan"));
        assertThat(game.getVisitor(), is("Air Force"));

        team.setName("Air Force");
        game = gp.getGame(team, 2017, 2).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Michigan"));
        assertThat(game.getVisitor(), is("Air Force"));
    }


    @Test
    public void MichiganBowlGame2016() {
        Team team = new Team();
        team.setName("Michigan");
        Game game = gp.getGame(team, 2016, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Florida State"));
        assertThat(game.getVisitor(), is("Michigan"));

        team.setName("Florida State");
        game = gp.getGame(team, 2016, 14).get();
        LOG.info(game.toString());
        assertThat(game.getHome(), is("Florida State"));
        assertThat(game.getVisitor(), is("Michigan"));

    }


    @Test
    public void AirForceByeWeek() {
        Team team = new Team();
        team.setName("Air Force");
        Optional<Game> opt = gp.getGame(team, 2017, 1);
        Assert.assertFalse("Should be Bye Week", opt.isPresent());
    }


    // @Test
    public void byTeamAndYear_Cincinnati2017() {

        List<Game> games = gp.byTeamAndYear("Rice", 2017)
                .collect(Collectors.toList());

        for (Game game : games) {
            LOG.info(game.toString());
        }
    }


}
