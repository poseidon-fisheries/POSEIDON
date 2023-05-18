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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        assertTrue(steps.shouldFish(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(TripRecord.class)
        ));

        when(fisher.getHoursAtSea()).thenReturn(50 * 24d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        //full, will be false
        assertFalse(steps.shouldFish(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(TripRecord.class)
        ));

        when(fisher.getHoursAtSea()).thenReturn(101 * 24d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(50d);
        //too late, will be false
        assertFalse(steps.shouldFish(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(TripRecord.class)
        ));


    }
}