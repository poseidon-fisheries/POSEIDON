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

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.SpaceOnlyKernelRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/27/16.
 */
public class SpaceOnlyKernelRegressionTest {


    @Test
    public void regress1d() throws Exception {


        SpaceOnlyKernelRegression regression = new SpaceOnlyKernelRegression(10, 3);

        SeaTile stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(0);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 100d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(10);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 150d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(7);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 110d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(15);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 200d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-3);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 20d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-6);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 0d), null, mock(FishState.class));

        //ought to be an increasing function
        for (int x = -10; x < 10; x++) {
            assertTrue(regression.predict(x, 0, 0d) > regression.predict(x - 1, 0, 0d));
        }

    }

    @Test
    public void regress2d() throws Exception {


        SpaceOnlyKernelRegression regression = new SpaceOnlyKernelRegression(10, 3);

        SeaTile stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(0);
        when(stub.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 100d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(10);
        when(stub.getGridY()).thenReturn(10);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 150d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(7);
        when(stub.getGridY()).thenReturn(7);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 110d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(15);
        when(stub.getGridY()).thenReturn(15);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 200d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-3);
        when(stub.getGridY()).thenReturn(-3);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 20d), null, mock(FishState.class));
        stub = mock(SeaTile.class);
        when(stub.getGridX()).thenReturn(-6);
        when(stub.getGridY()).thenReturn(-6);
        regression.addObservation(new GeographicalObservation<>(stub, 0, 0d), null, mock(FishState.class));

        //ought to be an increasing function
        for (int x = -10; x < 10; x++) {
            assertTrue(regression.predict(x, x, 0d) > regression.predict(x - 1, x - 1, 0d));
        }

    }

    @Test
    public void removeCorrectElement() throws Exception {


    }
}