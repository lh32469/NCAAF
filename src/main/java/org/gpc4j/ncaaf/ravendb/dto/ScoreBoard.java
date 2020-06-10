package org.gpc4j.ncaaf.ravendb.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.gpc4j.ncaaf.jaxb.Game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ScoreBoard extends HashMap {

  private String id;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Stream<Game> getGames() {

    final String[] array = id.split("\\.");
    final String season = array[1];
    final String week = array[2];

    List<Game> results = new LinkedList<>();
    ObjectMapper mapper = new ObjectMapper();

    JsonNode jsonNode = mapper.valueToTree(this);
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
