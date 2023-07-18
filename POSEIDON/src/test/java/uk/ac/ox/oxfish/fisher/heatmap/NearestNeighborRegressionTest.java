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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/1/16.
 */
public class NearestNeighborRegressionTest {


    @Test
    public void correctNeighbor() throws Exception {

        NearestNeighborRegression regression = new NearestNeighborRegression(1, 10000, 1);

        SeaTile tenten = mock(SeaTile.class);
        when(tenten.getGridX()).thenReturn(10);
        when(tenten.getGridY()).thenReturn(10);
        SeaTile zero = mock(SeaTile.class);
        when(zero.getGridX()).thenReturn(0);
        when(zero.getGridY()).thenReturn(0);
        regression.addObservation(new GeographicalObservation<>(tenten, 0, 100d), null, mock(FishState.class));
        regression.addObservation(new GeographicalObservation<>(zero, 0, 1d), null, mock(FishState.class));
        Assertions.assertEquals(regression.predict(0, 0, 0), 1, .001);
        Assertions.assertEquals(regression.predict(1, 0, 0), 1, .001);
        Assertions.assertEquals(regression.predict(0, 1, 0), 1, .001);
        Assertions.assertEquals(regression.predict(3, 3, 0), 1, .001);
        Assertions.assertEquals(regression.predict(6, 6, 0), 100, .001);
        Assertions.assertEquals(regression.predict(30, 30, 0), 100, .001);


    }
}