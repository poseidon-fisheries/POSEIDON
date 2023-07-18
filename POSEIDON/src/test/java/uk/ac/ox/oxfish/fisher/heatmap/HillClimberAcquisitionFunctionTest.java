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

package uk.ac.ox.oxfish.fisher.heatmap;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.HillClimberAcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/28/16.
 */
@SuppressWarnings("rawtypes")
public class HillClimberAcquisitionFunctionTest {


    //best spot is 25x25


    @Test
    public void hillclimbsTo2525() throws Exception {


        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        final FishState state = MovingTest.generateSimple50x50Map();
        when(state.getRandom()).thenReturn(random);
        when(state.getHoursSinceStart()).thenReturn(120d);

        final HillClimberAcquisitionFunction acquisitionFunction = new HillClimberAcquisitionFunction(1);
        final HillClimberAcquisitionFunction acquisitionFunction2 = new HillClimberAcquisitionFunction(3);

        final GeographicalRegression regression = mock(GeographicalRegression.class);
        when(regression.predict(any(SeaTile.class), eq(120d), any(), any())).thenAnswer((Answer<Double>) invocation -> {
            final SeaTile seaTile = (SeaTile) invocation.getArguments()[0];
            final double toReturn = -Math.abs(seaTile.getGridX() - 25) - Math.abs(seaTile.getGridY() - 25);
            return toReturn;
        });


        SeaTile pick = acquisitionFunction.pick(state.getMap(), regression, state, null, null);
        assertEquals(pick.getGridX(), 25);
        assertEquals(pick.getGridY(), 25);

        pick = acquisitionFunction2.pick(state.getMap(), regression, state, null, null);
        assertEquals(pick.getGridX(), 25);
        assertEquals(pick.getGridY(), 25);


    }
}