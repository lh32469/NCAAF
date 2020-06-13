package org.gpc4j.ncaaf.ravendb;

import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.providers.GamesProvider;
import org.gpc4j.ncaaf.providers.TeamProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gpc4j.ncaaf.ravendb.RavenGamesProvider.teamNameMatch;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


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
    assertThat(gp.getOpponent("Stanford", 2017, 1).get(),
        is("Southern California"));

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
    Game game = gp.getGame(team, 2016, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Florida St."));
    assertThat(game.getVisitor(), is("Michigan"));

    team.setName("Florida State");
    game = gp.getGame(team, 2016, 15).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Florida St."));
    assertThat(game.getVisitor(), is("Michigan"));

    List<Game> games = gp.byTeamAndYear("Michigan", 2019)
        .collect(Collectors.toList());

    System.out.println("games size = " + games.size());
    System.out.println("games = " + games);
  }


  @Test
  public void AirForceByeWeek() {
    Team team = new Team();
    team.setName("Air Force");
    Optional<Game> opt = gp.getGame(team, 2017, 1);
    assertFalse("Should be Bye Week", opt.isPresent());
  }

  @Test
  public void ohioState2019getGame() {
    final String team1 = "Ohio State";
    final String team2 = "Fla. Atlantic";
    final int season = 2019;
    final int week = 0;
    Game game = gp.getGame(team1, season, week).get();
    LOG.info(game.toString());
    assertThat(game.getHome(), is("Ohio St."));
    assertThat(game.getVisitor(), is(team2));

  }

  List<String> ohioStateOpponents = Arrays.asList(
      "Fla. Atlantic",
      "Cincinnati",
      "Indiana",
      "Miami (OH)",
      "Nebraska",
      "Michigan St.",
      null,
      "Northwestern",
      "Wisconsin",
      null,
      "Maryland",
      "Rutgers",
      "Penn St.",
      "Michigan",
      "Wisconsin",
      "Clemson");


  @Test
  public void ohioState2019() {
    final String fullName = "Ohio State";
    final String abbrevName = "Ohio St.";
    final int season = 2019;

    for (int week = 0; week < ohioStateOpponents.size(); week++) {
      String opponent = ohioStateOpponents.get(week);
      LOG.info("week = " + week + ", opponent = " + opponent);
      if (opponent == null) {
        // Bye Week
        assertFalse(gp.getOpponent(fullName, season, week).isPresent());
      } else {
        assertThat(gp.getOpponent(fullName, season, week).get(), is(opponent));
        assertThat(gp.getOpponent(opponent, season, week).get(), is(abbrevName));
      }
    }
  }


  @Test
  public void ohioSt2019() {
    final String team = "Ohio St.";
    final int season = 2019;

    for (int week = 0; week < ohioStateOpponents.size(); week++) {
      String opponent = ohioStateOpponents.get(week);
      if (opponent != null) {
        assertThat(gp.getOpponent(team, season, week).get(), is(opponent));
        assertThat(gp.getOpponent(opponent, season, week).get(), is(team));
        assertTrue(teamNameMatch(opponent).test(
            gp.getOpponent(team, season, week).get()));
        assertTrue(teamNameMatch(team).test(
            gp.getOpponent(opponent, season, week).get()));
      } else {
        // Bye Week
        assertFalse(gp.getOpponent(team, season, week).isPresent());
      }
    }
  }

  @Test
  public void IndianaVsOhioSt() {
    final String team1 = "Ohio State";
    final String team2 = "Indiana";
    final int season = 2019;
    final int week = 2;
    String opponent = gp.getOpponent(team1, season, week).get();
    assertThat(opponent, is(team2));
  }


  @Test
  public void LSU_2019() {
    final String team = "LSU";
    final int season = 2019;

    List<String> opponents = Arrays.asList(
        "Ga. Southern",
        "Texas",
        "Northwestern St.",
        "Vanderbilt",
        null,
        "Utah St.",
        "Florida",
        "Mississippi St.",
        "Auburn",
        null,
        "Alabama",
        "Ole Miss",
        "Arkansas",
        "Texas A&M",
        "Georgia",
        "Oklahoma",
        null,
        null);

    for (int week = 0; week < opponents.size(); week++) {
      String opponent = opponents.get(week);
      if (opponent != null) {
        assertThat(gp.getOpponent(team, season, week).get(), is(opponent));
        assertThat(gp.getOpponent(opponent, season, week).get(), is(team));
      } else {
        // Bye Week
        assertFalse(gp.getOpponent(team, season, week).isPresent());
      }
    }
  }

}
