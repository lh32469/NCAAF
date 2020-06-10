package org.gpc4j.ncaaf.ravendb;

import com.google.common.base.Strings;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.ncaaf.GamesProvider;
import org.gpc4j.ncaaf.XGame;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.ravendb.dto.ScoreBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RavenGamesProvider implements GamesProvider {

  final static private Logger LOG =
      LoggerFactory.getLogger(RavenGamesProvider.class);

  /**
   * Local cache of Games for a given Season.
   */
  private final Map<Integer, List<Game>> games = new HashMap<>();

  /**
   * Make copy of original Game as XGame.
   */
  private static final Function<Game, Game> clone = g -> (Game) new XGame(g);

  /**
   * Check if Game was played in the year provided.
   *
   * @param year
   * @return
   */
  private static Predicate<Game> played(Integer year) {
    return g -> !Strings.isNullOrEmpty(g.getHomeScore())
        && !Strings.isNullOrEmpty(g.getDate())
        && g.getDate().contains(year.toString());
  }

  /**
   * Check if the game involved the team name provided.
   *
   * @param teamName
   * @return
   */
  private static Predicate<Game> team(String teamName) {
    return g -> {

      boolean state = g.getHome().equals(teamName)
          || g.getVisitor().equals(teamName);

      String abbrev = teamName
          .replaceAll("State", "St.");
      boolean st = g.getHome().equals(abbrev)
          || g.getVisitor().equals(abbrev);

      return st || state;
    };
  }

  @Inject
  IDocumentStore docStore;


  @Override
  public void load() {

  }

  @Override
  public Stream<Game> getGames() {
    try (IDocumentSession session = docStore.openSession()) {

      List<ScoreBoard> scoreboards = session.query(ScoreBoard.class)
          .toList();

      List<Game> games = scoreboards.stream()
          .flatMap(ScoreBoard::getGames)
          .collect(Collectors.toList());

      return games.parallelStream().map(clone);
    }
  }

  @Override
  public Stream<Game> playedByYear(Integer season) {
    return getGames(season)
        .filter(played(season));
  }

  @Override
  public Stream<Game> byYear(Integer season) {
    return getGames(season);
  }

  @Override
  public Stream<Game> byTeam(String teamName) {
    return getGames()
        .filter(team(teamName));
  }

  @Override
  public Optional<Game> getGame(Team team, Integer year, int week) {
    return getGame(team.getName(), year, week);
  }

  @Override
  public Game lastGameOfYear(String teamName, Integer year) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Optional<Game> gameWithNoDate(String teamName, Integer keyYear) {
    return Optional.empty();
  }

  @Override
  public Stream<Game> byTeamAndYear(String teamName, Integer season) {
    return getGames(season)
        .filter(team(teamName));
  }

  @Override
  public Stream<Game> gamesPlayed(String teamName, Integer year) {
    return byTeamAndYear(teamName,year)
        .filter(played(year));
  }

  @Override
  public Optional<Game> getNextGame(String teamName, int year) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public Optional<Game> getGame(String teamName, Integer season, int week) {

    LOG.debug(teamName + "," + season + "," + week);

    String _week;

    week += 1;

    if (week < 10) {
      _week = "0" + week;
    } else if (week == 15) {
      _week = "P";
    } else {
      _week = String.valueOf(week);
    }
    return getGame(teamName, String.valueOf(season), _week);

  }

  public Optional<Game> getGame(String teamName, String season, String week) {

    LOG.debug(teamName + "," + season + "," + week);

    return byTeamAndYear(teamName, Integer.parseInt(season))
        .filter(g -> week.equalsIgnoreCase(g.getWeek()))
        .findFirst();
  }

  public Stream<Game> getGames(int season) {

    if (games.containsKey(season)) {
      return games.get(season).parallelStream().map(clone);
    }

    try (IDocumentSession session = docStore.openSession()) {

      List<ScoreBoard> scoreboards = session.query(ScoreBoard.class)
          .whereStartsWith("id", "scoreboard." + season)
          .toList();

      List<Game> games = scoreboards.stream()
          .flatMap(ScoreBoard::getGames)
          .collect(Collectors.toList());

      this.games.put(season, games);

      return games.parallelStream().map(clone);
    }

  }
}
