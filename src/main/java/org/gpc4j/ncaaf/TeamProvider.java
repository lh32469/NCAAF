package org.gpc4j.ncaaf;

import java.util.HashMap;
import java.util.Map;
import org.gpc4j.ncaaf.hystrix.GetTeamCommand;
import org.gpc4j.ncaaf.jaxb.Team;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 *
 * @author Lyle T Harris
 */
public class TeamProvider {

    private final JedisPool pool;

    private final Map<String, Team> teams = new HashMap<>();

    /**
     * Other names used by ESPN for the same Team.
     */
    private final Map<String, String> SUBSTITUTES;

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(TeamProvider.class);


    public TeamProvider(JedisPool pool) {
        this.pool = pool;
        Jedis jedis = pool.getResource();
        SUBSTITUTES = jedis.hgetAll("SUBSTITUTES");
        pool.returnResource(jedis);
    }


    public final void reset() {
        // Reset previous entries.
        teams.clear();
    }


    public synchronized Team getTeam(String teamName) {

        if (SUBSTITUTES.keySet().contains(teamName)) {
            teamName = SUBSTITUTES.get(teamName);
        }

        if (teams.keySet().contains(teamName)) {
            LOG.debug("Hit: " + teamName);
            Team team = teams.get(teamName);
            // Don't return actual object.
            return clone(team);
        } else {
            LOG.debug("Miss: " + teamName);
            Jedis jedis = pool.getResource();
            try {
                Team team = new GetTeamCommand(teamName, jedis).execute();
                teams.put(teamName, team);
                // Don't return actual object.
                return clone(team);
            } finally {
                pool.returnResource(jedis);
            }
        }
    }


    Team clone(Team team) {

        Team clone = new XTeam();
        clone.setName(team.getName());
        clone.setImage(team.getImage());

        return clone;
    }


}
