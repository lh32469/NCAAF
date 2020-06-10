package org.gpc4j.ncaaf.ravendb;

import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.TeamProvider;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * @author Lyle T Harris
 */
public class GamesProviderTest {


  final static private org.slf4j.Logger LOG
      = LoggerFactory.getLogger(GamesProviderTest.class);

  private static GamesProvider gp;


  @BeforeClass
  public static void setUpClass() {

    IDocumentStore store
        = new DocumentStore(
        "http://dell-4290.local:5050",
        "NCAAF");

    store.initialize();

    final AbstractBinder binder = new AbstractBinder() {
      @Override
      public void configure() {
        LOG.info("Configured");
        bindAsContract(TeamProvider.class);

        gp = new RavenGamesProvider();
        bind(gp).to(GamesProvider.class);
        bind(store).to(IDocumentStore.class);
      }
    };

    ServiceLocator locator = ServiceLocatorUtilities.bind(binder);
    locator.inject(gp);
  }


  @Test
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
    assertThat(game.getHome(), is("Southern California"));
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
    assertThat(game.getHome(), is("Florida St."));
    assertThat(game.getVisitor(), is("Michigan"));

    team.setName("Florida State");
    game = gp.getGame(team, 2016, 14).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Florida St."));
    assertThat(game.getVisitor(), is("Michigan"));

  }


  @Test
  public void AirForceByeWeek() {
    Team team = new Team();
    team.setName("Air Force");
    Optional<Game> opt = gp.getGame(team, 2017, 1);
    Assert.assertFalse("Should be Bye Week", opt.isPresent());
  }


}
