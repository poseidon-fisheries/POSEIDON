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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/29/17.
 */
public class MaxHoursPerYearDepartingStrategyTest {


    @Test
    public void maxHoursOut() throws Exception {
        Fisher fisher = mock(Fisher.class);
        MaxHoursPerYearDepartingStrategy strategy = new MaxHoursPerYearDepartingStrategy(100);
        when(fisher.getHoursAtSeaThisYear()).thenReturn(50d);

        Assertions.assertTrue(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));

        when(fisher.getHoursAtSeaThisYear()).thenReturn(150d);
        Assertions.assertFalse(strategy.shouldFisherLeavePort(fisher, mock(FishState.class), new MersenneTwisterFast()));


    }
}
