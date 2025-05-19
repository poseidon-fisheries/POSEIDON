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

package uk.ac.ox.oxfish.geography.currents;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;

import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.SECONDS_PER_DAY;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.metrePerSecondToXyPerDaysVector;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class CurrentVectorsFactoryTest {

    double EARTH_CIRCUMFERENCE = 40075.017;

    @Test
    public void testMetrePerSecondToXyPerDaysVector() {

        final MapExtent mapExtent = new MapExtentFactory(
            101, 100, -171, -70, -50, 50
        ).get();

        final double oneMeterPerSecondAtEquatorInDegreesPerDay =
            metrePerSecondToXyPerDaysVector(new Double2D(1, 0), new Coordinate(0, 0), mapExtent).length();

        Assertions.assertEquals(
            SECONDS_PER_DAY / ((EARTH_CIRCUMFERENCE / 360) * 1000),
            oneMeterPerSecondAtEquatorInDegreesPerDay,
            EPSILON
        );
    }
}
