package org.gpc4j.ncaaf.hystrix;

import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
public class TestCommand extends GetWeekCommand {


    public TestCommand(String key, JedisPool pool) {
        super(key, pool);
    }

}
