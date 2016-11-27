package org.gpc4j.ncaaf.hystrix;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.gpc4j.ncaaf.TeamProvider;
import static org.gpc4j.ncaaf.hystrix.HystrixProperties.REDIS_COMMAND_PROPS;
import static org.gpc4j.ncaaf.hystrix.HystrixProperties.REDIS_THREAD_PROPERTIES;
import org.gpc4j.ncaaf.jaxb.Team;
import org.gpc4j.ncaaf.jaxb.Week;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
public class GetWeekCommand extends HystrixCommand<Week> {

    private static final HystrixCommandGroupKey GROUP_KEY
            = HystrixCommandGroupKey.Factory
            .asKey(GetWeekCommand.class.getSimpleName());

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GetWeekCommand.class);

    private final JedisPool pool;

    private final String key;

    private final TeamProvider tp;


    public GetWeekCommand(String key, JedisPool pool, TeamProvider tp) {
        super(Setter
                .withGroupKey(GROUP_KEY)
                .andThreadPoolPropertiesDefaults(REDIS_THREAD_PROPERTIES)
                .andCommandPropertiesDefaults(REDIS_COMMAND_PROPS));
        this.key = key;
        this.pool = pool;
        this.tp = tp;
        LOG.debug(key);
    }


    @Override
    @Timed
    protected Week run() throws Exception {
        Week week = new Week();
        LOG.debug("Entering: " + key + "  Active: " + pool.getNumActive());

        Jedis jedis = pool.getResource();

        int y = 25;

        try {
            LOG.debug("Got Jedis Resource: " + key);
            if (jedis.exists(key)) {

                for (String teamName : jedis.lrange(key, 0, 100)) {

                    Team team = tp.getTeam(teamName);

                    team.setCX(week.getXPos());
                    team.setCY(y += 75);

                    week.getTeams().add(team);
                }
            }
        } finally {
            LOG.debug("Return Jedis Resource: " + key);
            pool.returnResource(jedis);
        }

        LOG.debug("Exiting: " + key);
        return week;
    }


}
