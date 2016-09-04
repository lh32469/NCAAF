package org.gpc4j.ncaaf;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author ltharris
 */
public class XGameTest {

    public XGameTest() {
    }


    @BeforeClass
    public static void setUpClass() {

    }


    @AfterClass
    public static void tearDownClass() {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    @Test
    public void finalScoresNonRanked() {
        String entry = "1=Hawaii 31   ^California 51 (FINAL)&ncf_s_right1_count=0&ncf_s_url1=http://espn.go.com/ncf/boxscore?gameId=400869090&";

        XGame game = new XGame(entry);
        assertEquals("GameId: ", "400869090", game.getId());
        assertEquals("VisitorRank: ", "", game.getVisitorRank());
        assertEquals("Visitor: ", "Hawaii", game.getVisitor());
        assertEquals("VisitorScore: ", "31", game.getVisitorScore());
        assertEquals("HomeRank: ", "", game.getHomeRank());
        assertEquals("Home: ", "California", game.getHome());
        assertEquals("HomeScore: ", "51", game.getHomeScore());
        assertNull(game.getDate());
    }


    @Test
    public void scheduledNonRanked() {
        String entry = "3=Presbyterian College at Central Michigan (THU, SEP 1 7:00 PM ET)&ncf_s_right3_count=0&ncf_s_url3=http://espn.go.com/ncf/preview?gameId=400869257&";

        XGame game = new XGame(entry);

        assertEquals("400869257", game.getId());
        assertEquals("", game.getVisitorRank());
        assertEquals("Presbyterian College", game.getVisitor());
        assertEquals("", game.getHomeRank());
        assertEquals("Central Michigan", game.getHome());
        assertEquals("2016-09-01T19:00", game.getDate());

        assertNull(game.getHomeScore());
        assertNull(game.getVisitorScore());
    }


    @Test
    public void scheduledTwoRankedTeams() {
        String entry = "30=(3) Oklahoma at (13) Houston (SAT, SEP 3 12:00 PM ET)&ncf_s_right30_count=0&ncf_s_url30=http://espn.go.com/ncf/preview?gameId=400869507&";
        XGame game = new XGame(entry);

        assertEquals("400869507", game.getId());
        assertEquals("3", game.getVisitorRank());
        assertEquals("Oklahoma", game.getVisitor());
        assertEquals("13", game.getHomeRank());
        assertEquals("Houston", game.getHome());
        assertEquals("2016-09-03T12:00", game.getDate());

        assertNull(game.getHomeScore());
        assertNull(game.getVisitorScore());
    }


    @Test
    public void visitorWinnerNonRanked() {
        String entry = "9=^Indiana 34   Florida Intl 13 (FINAL)&ncf_s_right9_count=0&ncf_s_url9=http://espn.go.com/ncf/boxscore?gameId=400869342&";
        XGame game = new XGame(entry);
        assertEquals("GameId: ", "400869342", game.getId());
        assertEquals("VisitorRank: ", "", game.getVisitorRank());
        assertEquals("Visitor: ", "Indiana", game.getVisitor());
        assertEquals("VisitorScore: ", "34", game.getVisitorScore());
        assertEquals("HomeRank: ", "", game.getHomeRank());
        assertEquals("Home: ", "Florida Intl", game.getHome());
        assertEquals("HomeScore: ", "13", game.getHomeScore());
        assertNull(game.getDate());
    }


    @Test
    public void miamiOfOhioFinal() {
        String entry = "46=Miami (OH) 21   ^(15) Iowa 45 (FINAL)&ncf_s_right46_count=0&ncf_s_url46=http://espn.go.com/ncf/boxscore?gameId=400869262&";
        XGame game = new XGame(entry);
        assertEquals("GameId: ", "400869262", game.getId());
        assertEquals("VisitorRank: ", "", game.getVisitorRank());
        assertEquals("Visitor: ", "Miami (OH)", game.getVisitor());
        assertEquals("VisitorScore: ", "21", game.getVisitorScore());
        assertEquals("HomeRank: ", "15", game.getHomeRank());
        assertEquals("Home: ", "Iowa", game.getHome());
        assertEquals("HomeScore: ", "45", game.getHomeScore());
        assertNull(game.getDate());
    }


}
