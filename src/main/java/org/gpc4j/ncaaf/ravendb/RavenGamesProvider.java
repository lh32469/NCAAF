package org.gpc4j.ncaaf.ravendb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.ncaaf.XGame;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.providers.GamesProvider;
import org.gpc4j.ncaaf.ravendb.dto.ScoreBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
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
    return g -> teamNameMatch(teamName).test(g.getHome())
        || teamNameMatch(teamName).test(g.getVisitor());
  }

  /**
   * Check if the teamName provided matches the other team name accounting
   * for substitutions such as St. for State.
   */
  static Predicate<String> teamNameMatch(String teamName) {
    return name -> {

      boolean state = name.equals(teamName);
      String abbrev = teamName
          .replaceAll("State", "St.");

      boolean st = name.equals(abbrev);

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
          .flatMap(this::getGames)
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
  public Stream<Game> byTeamAndYear(String teamName, Integer season) {
    return getGames(season)
        .filter(team(teamName));
  }

  @Override
  public Stream<Game> gamesPlayed(String teamName, Integer year) {
    return byTeamAndYear(teamName, year)
        .filter(played(year));
  }

  @Override
  public Optional<String> getOpponent(String teamName, int season, int week) {
    Optional<Game> optionalGame = getGame(teamName, season, week);
    if (optionalGame.isPresent()) {
      Game game = optionalGame.get();
      if (teamNameMatch(teamName).test(game.getVisitor())) {
        return Optional.of(game.getHome());
      } else {
        return Optional.of(game.getVisitor());
      }
    } else {
      return Optional.empty();
    }
  }


  @Override
  public Optional<Game> getGame(String teamName, int season, int week) {

    LOG.debug(teamName + "," + season + "," + week);

    String _week;

    week += 1;

    if (week < 10) {
      _week = "0" + week;
    } else if (week == 77) {
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
          .flatMap(this::getGames)
          .collect(Collectors.toList());

      this.games.put(season, games);

      return games.parallelStream().map(clone);
    }

  }


  Stream<Game> getGames(ScoreBoard scoreBoard) {

    final String[] array = scoreBoard.getId().split("\\.");
    final String season = array[1];
    String week = array[2];

    if ("P".equals(week)) {
      week = "16";
    }

    List<Game> results = new LinkedList<>();
    ObjectMapper mapper = new ObjectMapper();

    JsonNode jsonNode = mapper.valueToTree(scoreBoard);
    ArrayNode games = (ArrayNode) jsonNode.get("games");

    for (JsonNode node : games) {
      final JsonNode game = node.get("game");
      final JsonNode home = game.get("home");
      final JsonNode away = game.get("away");

      Game g = new Game();
      g.setId(game.get("gameID").textValue());
      g.setSeason(season);
      g.setWeek(week);

      g.setHome(home
          .get("names").get("short")
          .textValue().trim());
      g.setHomeScore(home
          .get("score")
          .textValue().trim());
      g.setHomeRank(home
          .get("rank")
          .textValue().trim());

      g.setVisitor(away
          .get("names").get("short")
          .textValue().trim());
      g.setVisitorScore(away
          .get("score")
          .textValue());
      g.setVisitorRank(away
          .get("rank")
          .textValue().trim());

      //System.out.println("Game = " + new XGame(g));
      results.add(g);
    }

    return results.stream();
  }
}
