package org.gpc4j.ncaaf;

import org.gpc4j.ncaaf.jaxb.Game;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author ltharris
 */
public class GameComparatorTest {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GameComparatorTest.class);


    @Test
    public void twoDates() {

        Game week1 = new Game();
        week1.setDate("2016-09-10T15:30");
        Game week0 = new Game();
        week0.setDate("2016-09-03T15:30");

        GameComparator instance = new GameComparator();
        int expResult = -7;
        int result = instance.compare(week0, week1);
        assertEquals(expResult, result);

        expResult = 7;
        result = instance.compare(week1, week0);
        assertEquals(expResult, result);
    }


    @Test
    public void firstGameNull() {
        GameComparator instance = new GameComparator();

        Game weekNull = new Game();

        Game week0 = new Game();
        week0.setDate("2016-09-03T15:30");

        int result = instance.compare(weekNull, week0);
        LOG.info("Result: " + result);
        assertTrue("Dates out of order", result > 0);

    }


    @Test
    public void secondGameNull() {
        GameComparator instance = new GameComparator();

        Game weekNull = new Game();

        Game week0 = new Game();
        week0.setDate("2016-09-03T15:30");

        int result = instance.compare(week0, weekNull);
        LOG.info("Result: " + result);
        assertTrue("Dates out of order", result < 0);

    }


    @Test
    public void twoNullGames() {
        GameComparator instance = new GameComparator();

        Game g1 = new Game();
        Game g2 = new Game();

        int result = instance.compare(g2, g1);
        LOG.info("Result: " + result);
        assertTrue("Dates out of order", result == 0);

    }


//    @Test
//    public void CincinnatiAtMichigan() {
//
//        GameComparator instance = new GameComparator();
//
//        Team team = new Team();
//        team.setName("Michigan");
//        Game g1 = GP.getGame(team, 2017, 1).get();
//
//        team.setName("Cincinnati");
//        Game g2 = GP.getGame(team, 2017, 1).get();
//
//        // Same Game
//        int result = instance.compare(g1, g2);
//        LOG.info(g1.toString());
//        LOG.info(g2.toString());
//        LOG.info("Result: " + result);
//        assertTrue("Dates out of order", result == 0);
//
//    }


}
