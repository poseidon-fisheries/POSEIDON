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

package uk.ac.ox.oxfish.fisher.heatmap;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.ExhaustiveAcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/28/16.
 */
@SuppressWarnings({"LocalCanBeFinal", "rawtypes"})
public class ExhaustiveAcquisitionFunctionTest {


    @Test
    public void finds2525() throws Exception {


        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        FishState state = MovingTest.generateSimple50x50Map();
        when(state.getRandom()).thenReturn(random);
        when(state.getHoursSinceStart()).thenReturn(120d);

        ExhaustiveAcquisitionFunction acquisitionFunction = new ExhaustiveAcquisitionFunction(1d, false, false);

        GeographicalRegression regression = mock(GeographicalRegression.class);
        when(regression.predict(any(SeaTile.class), eq(120d), any(), any())).thenAnswer((Answer<Double>) invocation -> {
            SeaTile seaTile = (SeaTile) invocation.getArguments()[0];
            double toReturn = -Math.abs(seaTile.getGridX() - 25) - Math.abs(seaTile.getGridY() - 25);
            return toReturn;
        });


        SeaTile pick = acquisitionFunction.pick(state.getMap(), regression, state, null, null);
        Assertions.assertEquals(pick.getGridX(), 25);
        Assertions.assertEquals(pick.getGridY(), 25);


    }

}
