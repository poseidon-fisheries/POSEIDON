package uk.ac.ox.oxfish.utility;

import org.jfree.util.Log;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 12/2/16.
 */
public class SeasonTest {


    @Test
    public void testsThatSeasonsAreCorrectlyGiven() throws Exception {

        Log.info("Tests that seasons are assigned correctly given the day number");

        assertEquals(Season.WINTER,Season.season(1));
        assertEquals(Season.WINTER,Season.season(40));
        assertEquals(Season.WINTER,Season.season(360));
        assertEquals(Season.SPRING,Season.season(90));
        assertEquals(Season.FALL,Season.season(280));
        assertEquals(Season.SUMMER,Season.season(190));



    }
}