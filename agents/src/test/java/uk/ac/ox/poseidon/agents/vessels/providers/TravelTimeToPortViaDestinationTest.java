/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.providers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.Optional;

import static org.mockito.Mockito.*;

class TravelTimeToPortViaDestinationTest {

    @Test
    void reflectsTheVesselsCurrentHomePortRatherThanASnapshot() {
        final Vessel vessel = mock(Vessel.class);
        final Engine engine = mock(Engine.class);
        final GridPathFinder pathFinder = mock(GridPathFinder.class);
        final DistanceCalculator distanceCalculator = mock(DistanceCalculator.class);

        final Int2D vesselCell = new Int2D(0, 0);
        final Int2D destination = new Int2D(1, 1);
        final Int2D firstPortCell = new Int2D(2, 2);
        final Int2D secondPortCell = new Int2D(3, 3);

        when(vessel.getEngine()).thenReturn(engine);
        when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
        when(vessel.getCell()).thenReturn(vesselCell);
        when(pathFinder.getPath(any(), any()))
            .thenReturn(Optional.of(ImmutableList.of(vesselCell, destination)));
        when(distanceCalculator.travelDuration(anyList(), anyDouble()))
            .thenReturn(java.time.Duration.ZERO);

        final TravelTimeToPortViaDestination travelTime =
            new TravelTimeToPortViaDestination(vessel, pathFinder, distanceCalculator);

        when(vessel.getHomePortLocation()).thenReturn(firstPortCell);
        travelTime.apply(destination);
        verify(pathFinder).getPath(destination, firstPortCell);

        when(vessel.getHomePortLocation()).thenReturn(secondPortCell);
        travelTime.apply(destination);
        verify(pathFinder).getPath(destination, secondPortCell);
    }
}
