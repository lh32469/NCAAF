package org.gpc4j.ncaaf.ravendb;

import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.gpc4j.ncaaf.providers.GamesProvider;
import org.gpc4j.ncaaf.providers.TeamProvider;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * @author Lyle T Harris
 */
public class BowlGamesTest {

  final static private org.slf4j.Logger LOG
      = LoggerFactory.getLogger(BowlGamesTest.class);

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
        bindAsContract(TeamProvider.class);
        bind(store).to(IDocumentStore.class);
      }
    };

    ServiceLocator locator = ServiceLocatorUtilities.bind(binder);
    gp = new RavenGamesProvider();
    locator.inject(gp);

    LOG.info("Configured");
  }


  @Test
  public void orangeBowlWisconsin_2017() {
    Team team = new Team();
    team.setName("Wisconsin");
    Game game = gp.getGame(team, 2017, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Miami (FL)"));
    assertThat(game.getVisitor(), is("Wisconsin"));
  }


  @Test
  public void orangeBowlMiami_2017() {
    Team team = new Team();
    team.setName("Miami (FL)");
    Game game = gp.getGame(team, 2017, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Miami (FL)"));
    assertThat(game.getVisitor(), is("Wisconsin"));
  }


  @Test
  public void roseBowlGeorgia_2018() {
    Team team = new Team();
    team.setName("Georgia");
    Game game = gp.getGame(team, 2017, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Oklahoma"));
    assertThat(game.getVisitor(), is("Georgia"));
  }


  @Test
  public void roseBowlOklahoma_2018() {
    Team team = new Team();
    team.setName("Oklahoma");
    Game game = gp.getGame(team, 2017, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Oklahoma"));
    assertThat(game.getVisitor(), is("Georgia"));
  }


  @Test
  public void sugarBowlClemson_2018() {
    Team team = new Team();
    team.setName("Clemson");
    Game game = gp.getGame(team, 2017, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Clemson"));
    assertThat(game.getVisitor(), is("Alabama"));
  }


  @Test
  public void sugarBowlAlabama_2018() {
    Team team = new Team();
    team.setName("Alabama");
    Game game = gp.getGame(team, 2017, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Clemson"));
    assertThat(game.getVisitor(), is("Alabama"));
  }


}
