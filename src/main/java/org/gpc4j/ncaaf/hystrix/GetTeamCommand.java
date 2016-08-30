package org.gpc4j.ncaaf.hystrix;

import com.google.common.base.Strings;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.util.Map;
import java.util.Optional;
import org.gpc4j.ncaaf.jaxb.Team;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;


/**
 *
 * @author Lyle T Harris
 */
public class GetTeamCommand extends HystrixCommand<Team> {

    private final String name;

    private final Jedis jedis;

    /**
     * Default, not found image.
     */
    private static final String image = "http://www.marook-online.de/tp-images/"
            + "1uid106189-3d-glossy-orange-orb-icon-signs-no-walking1.png";

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
            = LoggerFactory.getLogger(GetTeamCommand.class);


    /**
     * Get the named Team from Redis.
     *
     * @param name Name of the team to get.
     * @param jedis
     */
    public GetTeamCommand(String name, Jedis jedis) {
        super(Setter
                .withGroupKey(GetWeekCommand.GROUP_KEY)
                .andThreadPoolPropertiesDefaults(THREAD_PROPERTIES)
                .andCommandPropertiesDefaults(COMMAND_PROPS));
        this.name = name;
        this.jedis = jedis;
    }


    @Override
    protected Team run() throws Exception {

        LOG.debug(name);

        Map<String, String> data = jedis.hgetAll(name);

        if (data.isEmpty()) {
            throw new IllegalStateException(name + " not found");
        }

        Team team = new Team();
        team.setName(name);
        team.setImage(data.get("image"));

        return team;

    }


    @Override
    protected Team getFallback() {
        Team team = new Team();
        team.setName(name);
        team.setImage(image);
        return team;
    }


}
