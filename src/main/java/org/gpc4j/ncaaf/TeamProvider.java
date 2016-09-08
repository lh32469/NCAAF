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

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(TeamProvider.class);


    public TeamProvider(JedisPool pool) {
        this.pool = pool;
    }


    public final void reset() {
        // Reset previous entries.
        teams.clear();
    }


    public synchronized Team getTeam(String teamName) {
        if (teams.keySet().contains(teamName)) {
            LOG.debug("Hit: " + teamName);
            Team team = teams.get(teamName);
            return clone(team);
        } else {
            LOG.debug("Miss: " + teamName);
            Jedis jedis = pool.getResource();
            try {
                Team team = new GetTeamCommand(teamName, jedis).execute();
                teams.put(teamName, team);
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
        clone.setNext(team.getNext());
        clone.setNextGame(team.getNextGame());
        return clone;
    }


}
