package org.gpc4j.ncaaf.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.gpc4j.ncaaf.XGame;
import static org.gpc4j.ncaaf.hystrix.HystrixProperties.REDIS_COMMAND_PROPS;
import static org.gpc4j.ncaaf.hystrix.HystrixProperties.REDIS_THREAD_PROPERTIES;
import org.gpc4j.ncaaf.jaxb.Game;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
public class GetGameCommand extends HystrixCommand<Game> {

    private final String key;

    private final JedisPool pool;

    private static final HystrixCommandGroupKey GROUP_KEY
            = HystrixCommandGroupKey.Factory
            .asKey(GetGameCommand.class.getSimpleName());

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GetGameCommand.class);


    /**
     * Get the Game from Redis.
     *
     * @param key Redis key of the Game to load.
     * @param jedis
     */
    public GetGameCommand(String key, JedisPool pool) {

        super(Setter
                .withGroupKey(GROUP_KEY)
                .andThreadPoolPropertiesDefaults(REDIS_THREAD_PROPERTIES)
                .andCommandPropertiesDefaults(REDIS_COMMAND_PROPS));
        LOG.debug(key);
        this.key = key;
        this.pool = pool;
    }


    @Override
    protected Game run() throws Exception {

        Jedis jedis = pool.getResource();

        try {
            XGame game = new XGame(jedis.hgetAll(key));
            game.setId(key.replaceAll("game.2016.", ""));
            return game;
        } finally {
            pool.returnResource(jedis);
        }
    }


}
