package org.gpc4j.ncaaf.ravendb;

import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.ncaaf.TeamProvider;
import org.gpc4j.ncaaf.XTeam;
import org.gpc4j.ncaaf.jaxb.Team;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Lyle T Harris
 */
public class RavenTeamProvider implements TeamProvider {

  private List<Team> teams;

  @Inject
  IDocumentStore store;

  /**
   * Other names used by ESPN for the same Team.
   */
  private Map<String, String> substitutes;

  final static private org.slf4j.Logger LOG
      = LoggerFactory.getLogger(RavenTeamProvider.class);


  @PostConstruct
  public void postConstruct() {
    LOG.info("IDocumentStore: " + store);
    try (IDocumentSession session = store.openSession()) {
      teams = session.query(Team.class).toList();
      substitutes = (Map<String, String>) session.load(Map.class,
          "team-synonyms").get("names");
      LOG.info("Substitutes: " + substitutes);
    }
  }

  public final void reset() {
    // Reset previous entries.
    teams.clear();
  }

  public synchronized Team getTeam(String teamName) {

    Optional<Team> optional = teams.parallelStream()
        .filter(t -> t.getNames().contains(teamName))
        .findAny()
        .map(clone);

    if (optional.isPresent()) {
      LOG.trace("Found: '" + teamName + "'");
      return optional.get();
    } else {
      LOG.warn("Not Found: '" + teamName + "'");
      Team team2 = new XTeam();
      team2.setName(teamName);
      team2.setImage("");
      return team2;
    }

  }

  private static final Function<Team, Team> clone = t -> {
    Team team2 = new XTeam();
    team2.setName(t.getName());
    team2.setImage(t.getImage());
    team2.getNames().addAll(t.getNames());

    return team2;
  };

}
