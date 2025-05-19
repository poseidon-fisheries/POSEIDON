/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.plugins.FisherEntryByProfits;

import static org.mockito.Mockito.*;

public class FisherEntryByProfitsTest {


    @Test
    public void directTest() {

        FisherEntryByProfits profits = new FisherEntryByProfits("a", "b", "c",
            100, 100, 0
        );

        //10% returns
        int fishers = profits.newEntrants(100, 1000);
        //should be 10 fishers
        Assertions.assertEquals(fishers, 10);


        //0.1% returns: rounded away, no fishers
        fishers = profits.newEntrants(100, 1000000);
        Assertions.assertEquals(fishers, 0);

        //negative returns, no fishers
        fishers = profits.newEntrants(-100, 1000000);
        Assertions.assertEquals(fishers, 0);


        //NaN returns, no fishers
        fishers = profits.newEntrants(Double.NaN, 1000000);
        Assertions.assertEquals(fishers, 0);


    }


    @Test
    public void minProfits() {

        FisherEntryByProfits profits = new FisherEntryByProfits("a", "b", "c",
            100, 100,
            1000
        );

        //10% returns
        int fishers = profits.newEntrants(100, 1000);
        //There should be no new entrants because now you aren't even covering the profits you should cover
        Assertions.assertEquals(fishers, 0);


    }


    @Test
    public void testReadingComprehension() {
        FisherEntryByProfits profits = new FisherEntryByProfits("a", "b", "c", 100, 100,
            0
        );

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getLatestYearlyObservation("a")).thenReturn(100d);
        when(model.getLatestYearlyObservation("b")).thenReturn(1000d);

        profits.step(model);
        verify(model, times(10)).createFisher("c");

    }


    @Test
    public void pausedMeansNoNewFishers() {

        FisherEntryByProfits profits = new FisherEntryByProfits("a", "b", "c", 100, 100,
            0
        );

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getLatestYearlyObservation("a")).thenReturn(100d);
        when(model.getLatestYearlyObservation("b")).thenReturn(1000d);

        profits.setEntryPaused(true);
        profits.step(model);
        verify(model, times(0)).createFisher("c");

    }

}
