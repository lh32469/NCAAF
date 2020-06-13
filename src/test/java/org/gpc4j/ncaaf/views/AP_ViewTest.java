package org.gpc4j.ncaaf.views;

import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.gpc4j.ncaaf.providers.GamesProvider;
import org.gpc4j.ncaaf.PollProvider;
import org.gpc4j.ncaaf.providers.TeamProvider;
import org.gpc4j.ncaaf.XTeam;
import org.gpc4j.ncaaf.jaxb.Path;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.ravendb.RavenGamesProvider;
import org.gpc4j.ncaaf.ravendb.RavenTeamProvider;
import org.gpc4j.ncaaf.resources.AP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author ltharris
 */
public class AP_ViewTest {

  protected ServiceLocator locator;


  AP_View instance;
  AP ap = new AP();

  final static private Logger LOG
      = LoggerFactory.getLogger(AP_ViewTest.class);


  @After
  public void tearDown() {
    if (locator != null) {
      locator.shutdown();
    }
  }

  @Before
  public void setup() throws Exception {

    IDocumentStore store
        = new DocumentStore("http://dell-4290.local:5050", "NCAAF");

    store.initialize();

    final AbstractBinder binder = new AbstractBinder() {
      @Override
      public void configure() {
        bind(RavenTeamProvider.class).to(TeamProvider.class);
        bindAsContract(PollProvider.class);

        bind(RavenGamesProvider.class).to(GamesProvider.class);
        bind(store).to(IDocumentStore.class);

        instance = new AP_View();
        bind(instance).to(AP_View.class);
      }
    };

    locator = ServiceLocatorUtilities.bind(binder);

    locator.inject(instance);
    locator.inject(ap);
  }

//    @Test
//    public void getWeeks1() {
//        instance.setYear(2019);
//        List<Week> weeks = instance.getWeeksByScore();
//        System.out.println("weeks = " + weeks.size());
//        System.out.println("week0 = " + weeks.get(0).getTeams());
//        System.out.println("week1 = " + weeks.get(1).getTeams());
//    }

  @Test
  public void getPaths() {
    instance.setYear(2019);
    instance.setWeeks(ap.getWeeks(2019).collect(Collectors.toList()));
    List<Path> paths = instance.getPaths();
  }

  @Test
  public void getMichiganWeek0_2019() {

    instance.setYear(2019);
    Team team = new Team();
    team.setName("Michigan");
    team.getNames().add("Michigan");
    Team opp = instance.getOpponent(0, team);
    LOG.info("Opponent: " + opp.getName());
    assertThat(opp.getName(), is("Middle Tennessee"));
  }


  @Test
  public void michiganStateWeek0_2019() {

    instance.setYear(2019);
    Team team = new Team();
    team.setName("Michigan St.");
    team.getNames().add("Michigan St.");
    team.getNames().add("Michigan State");
    Team opp = instance.getOpponent(0, team);
    LOG.info("Opponent: " + opp.getName());
    assertThat(opp.getName(), is("Tulsa"));
  }

  @Test
  public void ohioStateWeek0_2019() {

    instance.setYear(2019);
    Team team = new XTeam();
    team.setName("Ohio St.");
    team.getNames().add("Ohio St.");
    team.getNames().add("Ohio State");
    LOG.info(team.toString());
    Team opp = instance.getOpponent(0, team);
    LOG.info("Opponent: " + opp);
    assertThat(opp.getName(), is("Florida Atlantic"));
  }

}
