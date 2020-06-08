package org.gpc4j.ncaaf;

import com.google.common.base.Strings;
import static java.time.DayOfWeek.SATURDAY;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.gpc4j.ncaaf.hystrix.GetGameCommand;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.jaxb.Team;
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
     * Check if Game was scheduled in the year provided.
     *
     * @param year
     */
    private static Predicate<Game> scheduled(Integer year) {
        return g -> !Strings.isNullOrEmpty(g.getDate())
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


    /**
     * Make copy of original Game as XGame.
     */
    private static final Function<Game, Game> clone = g -> (Game) new XGame(g);

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
                .map(clone) // Need to return clone of games
                .peek(anonymizeId());
    }


    /**
     * Get all completed games for the year.
     */
    public Stream<Game> playedByYear(Integer year) {

        return games.parallelStream()
                .filter(played(year))
                .map(clone) // Need to return clone of games
                .peek(anonymizeId());
    }


    /**
     * Get all games for the year whether they've been played or not.
     */
    public Stream<Game> byYear(Integer year) {

        return games.parallelStream()
                .filter(scheduled(year))
                .map(clone) // Need to return clone of games
                .peek(anonymizeId());
    }


    public Stream<Game> byTeam(String teamName) {

        return games.parallelStream()
                .filter(team(teamName))
                .map(clone) // Need to return clone of games
                .peek(anonymizeId());
    }


    public Optional<Game> getGame(Team team, Integer year, int week) {

        LOG.debug(team.getName() + "," + year + "," + week);
        Stream<Game> gg = byTeamAndYear(team.getName(), year);

        LocalDate _gDay = LocalDate.of(year, 9, 1)
                .with(TemporalAdjusters.firstDayOfMonth());

        LOG.debug("Season Starts: " + _gDay);

        for (int i = 0; i < week; i++) {
            _gDay = _gDay.plusDays(7);
        }

        final LocalDate gDay = _gDay;

        LOG.debug("GameDay: " + gDay);

        int _before = 4;
        int _after = 5;

        if (week == 0) {
            // For start of season, look up to three weeks before for 1st game.
            _before = 22;
        } else if (week == 14) {
            // For end of season, look up to four weeks after for last game.
            _after = 28;
        }

        // Make final for lambda 
        final int before = _before;
        final int after = _after;

        Optional<Game> optional
                = gg.filter(g -> {
                    LocalDate gDate
                            = LocalDateTime.parse(g.getDate()).toLocalDate();
                    return gDay.plusDays(after).isAfter(gDate)
                            && gDay.minusDays(before).isBefore(gDate);
                }).findFirst();

        if (!optional.isPresent() && week == 14) {
            // Feed from ESPN doesn't include dates 
            // for playoff games before they are played
            optional = gameWithNoDate(team.getName(), year);
            LOG.debug("Next: " + optional);
        }

        return optional;
    }


    public Game lastGameOfYear(String teamName, Integer year) {
        LOG.debug("Team: " + teamName + ", Year: " + year);
        Game g = byTeamAndYear(teamName, year)
                .reduce((first, second) -> second).get();
        LOG.debug("Game: " + g);
        return g;
    }


    /**
     * Get a Game for the named team that does not contain a date but whose key
     * matches the keyYear provided.
     */
    public Optional<Game> gameWithNoDate(String teamName, Integer keyYear) {
        LOG.debug("Team: " + teamName);
        Optional<Game> opt = byTeam(teamName)
                .filter(game -> game.getDate() == null)
                .filter(game -> game.getKey().contains(keyYear.toString()))
                .findFirst();

        LOG.debug("Game: " + opt);
        return opt;
    }


    public Stream<Game> byTeamAndYear(String teamName, Integer year) {

        // Don't use other methods to avoid needlessly creating Game clones
        // only to then filter them out.
        return games.parallelStream()
                .filter(team(teamName))
                .filter(scheduled(year))
                .map(clone) // Need to return clone of games
                //.peek(g -> LOG.info(g.toString()))
                .peek(anonymizeId());
    }


    /**
     * Get all the completed games for the named Team in the year provided.
     */
    public Stream<Game> gamesPlayed(String teamName, Integer year) {

        // Don't use other methods to avoid needlessly creating Game clones
        // only to then filter them out.
        return games.parallelStream()
                .filter(team(teamName))
                .filter(played(year))
                .map(clone) // Need to return clone of games
                .peek(anonymizeId());
    }


    public Optional<Game> getNextGame(String teamName, int year) {

        return games.stream()
                .filter(team(teamName))
                .filter(played(year).negate()) // Hasn't been played
                .map(clone) // Need to return clone of game
                .peek(anonymizeId())
                .findFirst();
    }


}
