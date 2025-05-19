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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MaximumDaysDecoratorTest {


    @Test
    public void maximumSteps() throws Exception {
        MaximumDaysDecorator steps = new MaximumDaysDecorator(100);

        //fish as long as it is BOTH not full and not being out for too long
        Fisher fisher = mock(Fisher.class);
        when(fisher.getMaximumHold()).thenReturn(100d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(50d);

        when(fisher.getHoursAtSea()).thenReturn(50 * 24d);

        //both are true
        Assertions.assertTrue(steps.shouldFish(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(TripRecord.class)
        ));

        when(fisher.getHoursAtSea()).thenReturn(50 * 24d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        //full, will be false
        Assertions.assertFalse(steps.shouldFish(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(TripRecord.class)
        ));

        when(fisher.getHoursAtSea()).thenReturn(101 * 24d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(50d);
        //too late, will be false
        Assertions.assertFalse(steps.shouldFish(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(TripRecord.class)
        ));


    }
}
