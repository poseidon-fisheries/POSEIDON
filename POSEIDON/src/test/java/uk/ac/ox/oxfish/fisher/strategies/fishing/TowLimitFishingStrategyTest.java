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
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.TowLimitFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/21/17.
 */
public class TowLimitFishingStrategyTest {


    @Test
    public void towing() throws Exception {


        TowLimitFactory factory = new TowLimitFactory();
        factory.setTowLimits(new FixedDoubleParameter(100d));
        FishingStrategy strategy = factory.apply(mock(FishState.class));
        TripRecord record = mock(TripRecord.class);
        when(record.getEffort()).thenReturn(1);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getMaximumHold()).thenReturn(100d);
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(),
            mock(FishState.class), record
        ));
        when(record.getEffort()).thenReturn(100);
        Assertions.assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(),
            mock(FishState.class), record
        ));

        when(record.getEffort()).thenReturn(101);
        Assertions.assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(),
            mock(FishState.class), record
        ));
    }
}
