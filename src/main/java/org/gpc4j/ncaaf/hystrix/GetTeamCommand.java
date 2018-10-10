package org.gpc4j.ncaaf.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import java.util.Map;
import org.gpc4j.ncaaf.XTeam;
import static org.gpc4j.ncaaf.hystrix.HystrixProperties.REDIS_COMMAND_PROPS;
import static org.gpc4j.ncaaf.hystrix.HystrixProperties.REDIS_THREAD_PROPERTIES;
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

    private static final HystrixCommandGroupKey GROUP_KEY
            = HystrixCommandGroupKey.Factory
            .asKey(GetTeamCommand.class.getSimpleName());

    /**
     * Default, not found image.
     */
    private static final String image = "http://www.marook-online.de/tp-images/"
            + "1uid106189-3d-glossy-orange-orb-icon-signs-no-walking1.png";

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
                .withGroupKey(GetTeamCommand.GROUP_KEY)
                .andThreadPoolPropertiesDefaults(REDIS_THREAD_PROPERTIES)
                .andCommandPropertiesDefaults(REDIS_COMMAND_PROPS));
        LOG.debug(name);
        this.name = name.trim();
        this.jedis = jedis;
    }


    @Override
    protected Team run() throws Exception {

        LOG.debug(name);

        Map<String, String> data = jedis.hgetAll(name);

        if (data.isEmpty()) {
            throw new IllegalStateException(name + " not found");
        }

        Team team = new XTeam();
        team.setName(name);
        team.setImage(data.get("image"));

        return team;

    }


    @Override
    protected Team getFallback() {
        LOG.warn("Couldn't Find: [" + name + "]");
        Team team = new Team();
        team.setName(name);
        team.setImage(image);
        return team;
    }


}
