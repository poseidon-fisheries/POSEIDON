/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;

public class TwoByTwoRegionalDivisionTest {

    @Test
    public void testLocationsInDivision() {
        final MapExtent mapExtent = new MapExtentFactory(
            101, 100, -171, -70, -50, 50
        ).get();

        final TwoByTwoRegionalDivision division =
            new TwoByTwoRegionalDivision(new Coordinate(-140.5, 0.5), mapExtent);

        final ImmutableMap<Coordinate, String> testPoints =
            new ImmutableMap.Builder<Coordinate, String>()
                .put(new Coordinate(-149.5, 49.5), "Northwest")
                .put(new Coordinate(-140.5, 49.5), "Northwest")
                .put(new Coordinate(-149.5, 0.5), "Northwest")
                .put(new Coordinate(-140.5, 0.5), "Northwest")
                .put(new Coordinate(-149.5, -49.5), "Southwest")
                .put(new Coordinate(-140.5, -49.5), "Southwest")
                .put(new Coordinate(-149.5, -0.5), "Southwest")
                .put(new Coordinate(-140.5, -0.5), "Southwest")
                .put(new Coordinate(-139.5, 49.5), "Northeast")
                .put(new Coordinate(-70.5, 49.5), "Northeast")
                .put(new Coordinate(-139.5, 0.5), "Northeast")
                .put(new Coordinate(-70.5, 0.5), "Northeast")
                .put(new Coordinate(-139.5, -49.5), "Southeast")
                .put(new Coordinate(-70.5, -49.5), "Southeast")
                .put(new Coordinate(-139.5, -0.5), "Southeast")
                .put(new Coordinate(-70.5, -0.5), "Southeast")
                .build();

        testPoints.forEach(((coordinate, regionName) ->
            Assertions.assertEquals(regionName, division.getRegion(coordinate).getName(), coordinate.toString())
        ));

    }
}
