package org.gpc4j.ncaaf;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.gpc4j.ncaaf.hystrix.GetGameCommand;
import org.gpc4j.ncaaf.jaxb.Game;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
public class GamesProvider {

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
        return g -> g.getHome().equals(teamName)
                || g.getVisitor().equals(teamName);
    }


    /**
     * Set the Id of the Game to something other than the ESPN Id but related to
     * it.
     *
     * @return
     */
    private static Consumer<Game> anonymizeId() {
        return g -> {
            String origId = g.getId();
            int hash = origId.hashCode() * 31;
            hash = Math.abs(hash);
            g.setId(Integer.toHexString(hash));
        };
    }


    private final JedisPool pool;

    /**
     * Use ArrayList as it's better for parallel Streams access.
     */
    private final List<Game> games = new ArrayList<>();

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GamesProvider.class);


    public GamesProvider(JedisPool pool) {
        this.pool = pool;
        load();
    }


    public final void load() {

        // Reset previous entries.
        games.clear();

        Jedis jedis = pool.getResource();
        try {
            for (String key : jedis.keys("game.*")) {
                games.add(new GetGameCommand(key, pool).execute());
            }
            LOG.info("Games Loaded.");
        } finally {
            pool.returnResource(jedis);
        }

        Collections.sort(games, new GameComparator());
    }


    public Stream<Game> getGames() {

        return games.parallelStream()
                .map(g -> (Game) new XGame(g)) // Need to return clone of games
                .peek(anonymizeId());
    }


    public Stream<Game> byYear(Integer year) {

        return games.parallelStream()
                .filter(played(year))
                .map(g -> (Game) new XGame(g)) // Need to return clone of games
                .peek(anonymizeId());
    }


    public Stream<Game> byTeam(String teamName) {

        return games.parallelStream()
                .filter(team(teamName))
                .map(g -> (Game) new XGame(g)) // Need to return clone of games
                .peek(anonymizeId());
    }


    public Game lastGameOfYear(String teamName, Integer year) {
        LOG.debug("Team: " + teamName + ", Year: " + year);
        Game g = byTeamAndYear(teamName, year)
                .reduce((first, second) -> second).get();
        LOG.debug("Game: " + g);
        return g;
    }


    public Stream<Game> byTeamAndYear(String teamName, Integer year) {

        // Don't use other methods to avoid needlessly creating Game clones
        // only to then filter them out.
        return games.parallelStream()
                .filter(team(teamName))
                .filter(played(year))
                .map(g -> (Game) new XGame(g)) // Need to return clone of games
                .peek(anonymizeId());
    }


}
