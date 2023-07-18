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

package uk.ac.ox.oxfish.geography;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/30/16.
 */
public class CentroidMapDiscretizerTest {

    @Test
    public void centroid() {
        final FishState state = buildState();
        final CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(
            Lists.newArrayList(
                new Coordinate(0, 1),
                new Coordinate(1, 0)
            )
        );
        final MapDiscretization discretization = new MapDiscretization(discretizer);
        checkDiscretization(state, discretization, 0);
    }

    private FishState buildState() {
        final FishState state = MovingTest.generateSimple4x4Map();
        state.getMap().recomputeTilesMPA();
        assertEquals(new Coordinate(0.125, .875, 0), state.getMap().getCoordinates(0, 0));
        assertEquals(new Coordinate(.875, 0.125, 0), state.getMap().getCoordinates(3, 3));
        return state;
    }

    private void checkDiscretization(
        final FishState state,
        final MapDiscretization discretization,
        final Integer tileZeroZeroGroup
    ) {
        discretization.discretize(state.getMap());
        assertEquals(tileZeroZeroGroup, discretization.getGroup(state.getMap().getSeaTile(0, 0)));
        assertEquals(0, (int) discretization.getGroup(state.getMap().getSeaTile(1, 1)));
        assertEquals(0, (int) discretization.getGroup(state.getMap().getSeaTile(0, 1)));
        assertEquals(1, (int) discretization.getGroup(state.getMap().getSeaTile(2, 2)));
        assertEquals(1, (int) discretization.getGroup(state.getMap().getSeaTile(3, 3)));
    }

    //same as above, but builds the map from factory
    @Test
    public void centroidFromFactory() {
        final FishState state = buildState();
        final CentroidMapFileFactory factory = new CentroidMapFileFactory();
        factory.setFilePath(Paths.get("inputs", "tests", "fake_centroids.txt").toString());
        factory.setxColumnName("x");
        factory.setyColumnName("y");
        final MapDiscretization discretization = new MapDiscretization(factory.apply(mock(FishState.class)));
        checkDiscretization(state, discretization, 0);
    }

    //like above but not allowing 0,0 to be grouped
    @Test
    public void centroidFiltered() {

        final FishState state = buildState();

        final CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(
            Lists.newArrayList(
                new Coordinate(0, 1),
                new Coordinate(1, 0)
            )
        );
        discretizer.addFilter(tile -> tile.getGridY() > 0 || tile.getGridX() > 0);

        final MapDiscretization discretization = new MapDiscretization(discretizer);
        checkDiscretization(state, discretization, null);

    }

}