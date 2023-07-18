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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborTransductionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborTransduction;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 7/5/16.
 */
public class NearestNeighborTransductionTest {

    @Test
    public void correctNeighbor() throws Exception {

        FishState state = MovingTest.generateSimple50x50Map();
        NearestNeighborTransduction regression = (new NearestNeighborTransductionFactory()).apply(state);
        regression.addObservation(
            new GeographicalObservation<>(state.getMap().getSeaTile(10, 10), 0, 100d),
            null,
            null
        );
        regression.addObservation(new GeographicalObservation<>(state.getMap().getSeaTile(0, 0), 0, 1d), null, null);
        assertEquals(regression.predict(state.getMap().getSeaTile(0, 0), 0, null, null), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(1, 0), 0, null, null), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(0, 1), 0, null, null), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(3, 3), 0, null, null), 1d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(6, 6), 0, null, null), 100d, .001);
        assertEquals(regression.predict(state.getMap().getSeaTile(30, 30), 0, null, null), 100d, .001);


    }

}