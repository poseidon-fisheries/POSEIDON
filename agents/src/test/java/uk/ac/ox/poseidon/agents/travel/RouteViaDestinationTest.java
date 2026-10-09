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

package uk.ac.ox.poseidon.agents.travel;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import sim.util.Number2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouteViaDestinationTest {

    private final Int2D vesselCell = new Int2D(0, 0);
    private final Int2D waypoint = new Int2D(1, 0);
    private final Int2D destination = new Int2D(1, 1);
    private final Int2D portCell = new Int2D(2, 2);
    private final Vessel vessel = mock(Vessel.class);
    private final GridPathFinder pathFinder = mock(GridPathFinder.class);
    private final DistanceCalculator distanceCalculator = mock(DistanceCalculator.class);
    private final RouteViaDestination routeViaDestination =
        new RouteViaDestination(vessel, pathFinder, distanceCalculator);

    @BeforeEach
    void setUp() {
        final Engine engine = mock(Engine.class);
        when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
        when(vessel.getEngine()).thenReturn(engine);
        when(vessel.getCell()).thenReturn(vesselCell);
        when(vessel.getHomePortLocation()).thenReturn(portCell);
        when(distanceCalculator.distanceInKm(any(Number2D.class), any(Number2D.class)))
            .thenReturn(5.0);
        when(pathFinder.getPath(vesselCell, destination))
            .thenReturn(Optional.of(ImmutableList.of(vesselCell, waypoint, destination)));
        when(pathFinder.getPath(destination, portCell))
            .thenReturn(Optional.of(ImmutableList.of(destination, portCell)));
    }

    @Test
    void addsUpTheLegsToTheDestinationAndBackToPort() {
        final Route route = routeViaDestination.apply(destination);

        assertThat(route.getDistanceInKm()).isEqualTo(15.0);
        assertThat(route.getDuration()).isEqualTo(Duration.ofMinutes(90));
    }

    @Test
    void readsTheVesselsCurrentCellAndHomePortAtEachCall() {
        final Int2D otherPortCell = new Int2D(3, 3);
        when(vessel.getCell()).thenReturn(destination);
        when(vessel.getHomePortLocation()).thenReturn(otherPortCell);
        when(pathFinder.getPath(destination, destination))
            .thenReturn(Optional.of(ImmutableList.of(destination)));
        when(pathFinder.getPath(destination, otherPortCell))
            .thenReturn(Optional.of(ImmutableList.of(destination, waypoint, otherPortCell)));

        assertThat(routeViaDestination.apply(destination).getDistanceInKm()).isEqualTo(10.0);
    }

    @Test
    void aVesselAlreadyAtTheDestinationOnlyTravelsBack() {
        when(vessel.getCell()).thenReturn(destination);
        when(pathFinder.getPath(destination, destination))
            .thenReturn(Optional.of(ImmutableList.of(destination)));

        final Route route = routeViaDestination.apply(destination);

        assertThat(route.getDistanceInKm()).isEqualTo(5.0);
        assertThat(route.getDuration()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void throwsWhenThereIsNoPath() {
        when(pathFinder.getPath(destination, portCell)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeViaDestination.apply(destination))
            .isInstanceOf(IllegalStateException.class);
    }
}
