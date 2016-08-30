package org.gpc4j.ncaaf;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.gpc4j.ncaaf.hystrix.GetGameCommand;
import org.gpc4j.ncaaf.jaxb.Game;
import org.gpc4j.ncaaf.resources.AP;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
public class GamesProvider {

    private final JedisPool pool;

    private final List<Game> games = new LinkedList<>();

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GamesProvider.class);


    public GamesProvider(JedisPool pool) {
        this.pool = pool;
        load();
    }


    public final void load() {
        Jedis jedis = pool.getResource();
        try {
            for (String key : jedis.keys("game.2016.*")) {
                games.add(new GetGameCommand(key, pool).execute());
            }
            LOG.info("Games Loaded.");
        } finally {
            pool.returnResource(jedis);
        }
    }


    public Stream<Game> getGames() {
        return games.parallelStream();
    }


}
