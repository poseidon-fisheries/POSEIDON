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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.SimpleKalmanRegression;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 8/3/16.
 */
public class SimpleKalmanRegressionTest {


    @Test
    public void kalman() throws Exception {
        FishState state = MovingTest.generateSimple50x50Map();

        SimpleKalmanRegression regression = new SimpleKalmanRegression(
            10, 1, 0, 100, 50, .2, 0, 0, state.getMap(), new MersenneTwisterFast());

        regression.addObservation(
            new GeographicalObservation<>(state.getMap().getSeaTile(10, 10), 0, 100d),
            null,
            mock(FishState.class)
        );
        regression.addObservation(
            new GeographicalObservation<>(state.getMap().getSeaTile(5, 5), 0, 50d),
            null,
            mock(FishState.class)
        );
        regression.addObservation(
            new GeographicalObservation<>(state.getMap().getSeaTile(3, 3), 0, 30d),
            null,
            mock(FishState.class)
        );
        regression.addObservation(
            new GeographicalObservation<>(state.getMap().getSeaTile(0, 0), 0, 1d),
            null,
            mock(FishState.class)
        );
        //should smooth somewhat linearly
        Assertions.assertTrue(regression.predict(state.getMap().getSeaTile(0, 0), 0, null, mock(FishState.class)) <
            regression.predict(state.getMap().getSeaTile(10, 10), 0, null, mock(FishState.class)));
        Assertions.assertTrue(regression.predict(state.getMap().getSeaTile(0, 0), 0, null, mock(FishState.class)) <
            regression.predict(state.getMap().getSeaTile(2, 2), 0, null, mock(FishState.class)));
        Assertions.assertTrue(regression.predict(state.getMap().getSeaTile(2, 2), 0, null, mock(FishState.class)) <
            regression.predict(state.getMap().getSeaTile(5, 5), 0, null, mock(FishState.class)));
        Assertions.assertTrue(regression.predict(state.getMap().getSeaTile(5, 5), 0, null, mock(FishState.class)) <
            regression.predict(state.getMap().getSeaTile(10, 10), 0, null, mock(FishState.class)));


    }
}
