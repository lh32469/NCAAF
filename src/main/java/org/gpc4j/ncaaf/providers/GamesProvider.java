package org.gpc4j.ncaaf.providers;

import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;

import java.util.Optional;
import java.util.stream.Stream;

public interface GamesProvider {
  void load();

  Stream<Game> getGames();

  Stream<Game> playedByYear(Integer year);

  Stream<Game> byYear(Integer year);

  Stream<Game> byTeam(String teamName);

  Optional<Game> getGame(Team team, Integer year, int week);

  /**
   * Get the Game for the named Team for the given season and week.
   */
  Optional<Game> getGame(String teamName, int season, int week);

  Stream<Game> byTeamAndYear(String teamName, Integer year);

  Stream<Game> gamesPlayed(String teamName, Integer year);

  /**
   * Get the opposing Team's name for the given season and week.
   */
  Optional<String> getOpponent(String teamName, int season, int week);

}
