package org.gpc4j.ncaaf;

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

  Game lastGameOfYear(String teamName, Integer year);

  Optional<Game> gameWithNoDate(String teamName, Integer keyYear);

  Stream<Game> byTeamAndYear(String teamName, Integer year);

  Stream<Game> gamesPlayed(String teamName, Integer year);

  Optional<Game> getNextGame(String teamName, int year);
}
