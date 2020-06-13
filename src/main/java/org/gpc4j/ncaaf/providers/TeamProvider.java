package org.gpc4j.ncaaf.providers;

import org.gpc4j.ncaaf.jaxb.Team;

public interface TeamProvider {

  Team getTeam(String teamName);

  void reset();
}
