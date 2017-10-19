/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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