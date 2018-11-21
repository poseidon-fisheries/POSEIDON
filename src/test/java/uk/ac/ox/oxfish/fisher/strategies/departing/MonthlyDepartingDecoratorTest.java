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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MonthlyDepartingDecoratorTest {

    @Test
    public void monthsMatter() throws Exception {


        //january and may
        MonthlyDepartingDecorator strategy = new MonthlyDepartingDecorator(new MaxHoursPerYearDepartingStrategy(999999),
                                                                           0, 4);

        //you can depart in january
        FishState state = mock(FishState.class);
        when(state.getDayOfTheYear()).thenReturn(1);
        assertTrue(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


        when(state.getDayOfTheYear()).thenReturn(20);
        assertTrue(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));




        //cannot depart february
        when(state.getDayOfTheYear()).thenReturn(40);
        assertFalse(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));



        //can depart May
        when(state.getDayOfTheYear()).thenReturn(140);
        assertTrue(
                strategy.shouldFisherLeavePort(mock(Fisher.class), state, new MersenneTwisterFast()));


    }
}