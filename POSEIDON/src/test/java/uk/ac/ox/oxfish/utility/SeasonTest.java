/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility;

import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by carrknight on 12/2/16.
 */
public class SeasonTest {


    @Test
    public void testsThatSeasonsAreCorrectlyGiven() throws Exception {

        Log.info("Tests that seasons are assigned correctly given the day number");

        Assertions.assertEquals(Season.WINTER, Season.season(1));
        Assertions.assertEquals(Season.WINTER, Season.season(40));
        Assertions.assertEquals(Season.WINTER, Season.season(360));
        Assertions.assertEquals(Season.SPRING, Season.season(90));
        Assertions.assertEquals(Season.FALL, Season.season(280));
        Assertions.assertEquals(Season.SUMMER, Season.season(190));


    }
}
