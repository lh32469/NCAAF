package org.gpc4j.ncaaf.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.util.logging.Logger;
import org.gpc4j.ncaaf.XGame;
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

    static final HystrixCommandGroupKey GROUP_KEY
            = HystrixCommandGroupKey.Factory.asKey("Redis");

    private static final HystrixCommandProperties.Setter COMMAND_PROPS
            = HystrixCommandProperties.Setter()
            .withExecutionTimeoutInMilliseconds(300000);

    private static final HystrixThreadPoolProperties.Setter THREAD_PROPERTIES
            = HystrixThreadPoolProperties.Setter()
            .withQueueSizeRejectionThreshold(10000)
            .withMaxQueueSize(1000);

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
                .withGroupKey(GetWeekCommand.GROUP_KEY)
                .andThreadPoolPropertiesDefaults(THREAD_PROPERTIES)
                .andCommandPropertiesDefaults(COMMAND_PROPS));
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
