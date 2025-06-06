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


public class MonthlyDepartingDecoratorTest {

    @Test
    public void monthsMatter() throws Exception {


        //january and may
        MonthlyDepartingDecorator strategy = new MonthlyDepartingDecorator(new MaxHoursPerYearDepartingStrategy(999999),
            0, 4
        );

        //you can depart in january
        FishState state = mock(FishState.class);
        when(state.getDayOfTheYear()).thenReturn(1);
        Assertions.assertTrue(strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


        when(state.getDayOfTheYear()).thenReturn(20);
        Assertions.assertTrue(strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


        //cannot depart february
        when(state.getDayOfTheYear()).thenReturn(40);
        Assertions.assertFalse(strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


        //can depart May
        when(state.getDayOfTheYear()).thenReturn(140);
        Assertions.assertTrue(strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


    }
}
