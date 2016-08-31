package org.gpc4j.ncaaf.hystrix;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.gpc4j.ncaaf.TeamProvider;
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

    static final HystrixCommandGroupKey GROUP_KEY
            = HystrixCommandGroupKey.Factory.asKey("Redis");

    private static final HystrixCommandProperties.Setter COMMAND_PROPS
            = HystrixCommandProperties.Setter()
            .withExecutionTimeoutInMilliseconds(300000);

    private static final HystrixThreadPoolProperties.Setter THREAD_PROPERTIES
            = HystrixThreadPoolProperties.Setter()
            .withQueueSizeRejectionThreshold(10000)
            .withMaxQueueSize(1000);

    /**
     * Default, not found image.
     */
    private String image = "http://www.marook-online.de/tp-images/"
            + "1uid106189-3d-glossy-orange-orb-icon-signs-no-walking1.png";

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GetWeekCommand.class);

    private final JedisPool pool;

    private final String key;
    private final TeamProvider tp;


    public GetWeekCommand(String key, JedisPool pool, TeamProvider tp) {
        super(Setter
                .withGroupKey(GetWeekCommand.GROUP_KEY)
                .andThreadPoolPropertiesDefaults(THREAD_PROPERTIES)
                .andCommandPropertiesDefaults(COMMAND_PROPS));
        this.key = key;
        this.pool = pool;
        this.tp = tp;
        LOG.debug(key);
    }


    @Override
    @Timed
    protected Week run() throws Exception {
        Week week = new Week();

        Jedis jedis = pool.getResource();
        int y = 25;

        try {
            if (jedis.exists(key)) {

                for (String teamName : jedis.lrange(key, 0, 100)) {

                    Team team = tp.getTeam(teamName);

                    team.setCX(week.getXPos());
                    team.setCY(y += 75);

                    week.getTeams().add(team);
                }
            }
        } finally {
            pool.returnResource(jedis);
        }

        return week;
    }


}
