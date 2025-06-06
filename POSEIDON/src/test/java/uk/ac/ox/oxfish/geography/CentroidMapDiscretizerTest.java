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

package uk.ac.ox.oxfish.geography;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Paths;

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
        Assertions.assertEquals(new Coordinate(0.125, .875, 0), state.getMap().getCoordinates(0, 0));
        Assertions.assertEquals(new Coordinate(.875, 0.125, 0), state.getMap().getCoordinates(3, 3));
        return state;
    }

    private void checkDiscretization(
        final FishState state,
        final MapDiscretization discretization,
        final Integer tileZeroZeroGroup
    ) {
        discretization.discretize(state.getMap());
        Assertions.assertEquals(tileZeroZeroGroup, discretization.getGroup(state.getMap().getSeaTile(0, 0)));
        Assertions.assertEquals(0, (int) discretization.getGroup(state.getMap().getSeaTile(1, 1)));
        Assertions.assertEquals(0, (int) discretization.getGroup(state.getMap().getSeaTile(0, 1)));
        Assertions.assertEquals(1, (int) discretization.getGroup(state.getMap().getSeaTile(2, 2)));
        Assertions.assertEquals(1, (int) discretization.getGroup(state.getMap().getSeaTile(3, 3)));
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
