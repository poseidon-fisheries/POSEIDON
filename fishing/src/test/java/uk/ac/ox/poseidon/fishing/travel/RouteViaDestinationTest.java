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

package uk.ac.ox.poseidon.fishing.travel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentGiving;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;

class RouteViaDestinationTest {

  private final Cell vesselCell = new Cell(0, 0);
  private final Cell waypoint = new Cell(1, 0);
  private final Cell destination = new Cell(1, 1);
  private final Cell portCell = new Cell(2, 2);
  private final Vessel vessel = mock(Vessel.class);
  private final Engine engine = mock(Engine.class);
  private final GridPathFinder pathFinder = mock(GridPathFinder.class);
  private final DistanceCalculator distanceCalculator = mock(
    DistanceCalculator.class
  );
  private final VesselComponent<Engine> engineComponent = componentGiving(
    vessel,
    engine
  );
  private final RouteViaDestination routeViaDestination =
    new RouteViaDestination(
      engineComponent,
      new PathLengthCalculator(pathFinder, distanceCalculator)
    );

  @BeforeEach
  void setUp() {
    when(pathFinder.getModelGrid()).thenReturn(
      ModelGrid.create(4, 4, new Envelope(0, 4, 0, 4))
    );
    when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
    when(vessel.getCell()).thenReturn(vesselCell);
    when(vessel.getHomePortCell()).thenReturn(portCell);
    when(
      distanceCalculator.distanceInKm(any(Cell.class), any(Cell.class))
    ).thenReturn(5.0);
    when(distanceCalculator.distanceInKm(anyList())).thenCallRealMethod();
    when(pathFinder.getPath(vesselCell, destination)).thenReturn(
      Optional.of(ImmutableList.of(vesselCell, waypoint, destination))
    );
    when(pathFinder.getPath(destination, portCell)).thenReturn(
      Optional.of(ImmutableList.of(destination, portCell))
    );
  }

  @Test
  void addsUpTheLegsToTheDestinationAndBackToPort() {
    final Route route = routeViaDestination.apply(vessel, destination);

    assertThat(route.getDistanceInKm()).isEqualTo(15.0);
    assertThat(route.getDuration()).isEqualTo(Duration.ofMinutes(90));
  }

  @Test
  void readsTheVesselsCurrentCellHomePortAndSpeedAtEachCall() {
    final Route routeBefore = routeViaDestination.apply(vessel, destination);

    final Cell otherPortCell = new Cell(3, 3);
    when(vessel.getCell()).thenReturn(destination);
    when(vessel.getHomePortCell()).thenReturn(otherPortCell);
    when(engine.getCruisingSpeedInKph()).thenReturn(20.0);
    when(pathFinder.getPath(destination, destination)).thenReturn(
      Optional.of(ImmutableList.of(destination))
    );
    when(pathFinder.getPath(destination, otherPortCell)).thenReturn(
      Optional.of(ImmutableList.of(destination, waypoint, otherPortCell))
    );
    final Route routeAfter = routeViaDestination.apply(vessel, destination);

    assertThat(routeBefore).isEqualTo(new Route(15.0, Duration.ofMinutes(90)));
    assertThat(routeAfter).isEqualTo(new Route(10.0, Duration.ofMinutes(30)));
  }

  @Test
  void givesTheRouteOfTheVesselGiven() {
    final Vessel otherVessel = mock(Vessel.class);
    final Engine otherEngine = mock(Engine.class);
    when(otherEngine.getCruisingSpeedInKph()).thenReturn(20.0);
    giveComponent(otherVessel, engineComponent, otherEngine);
    when(otherVessel.getCell()).thenReturn(destination);
    when(otherVessel.getHomePortCell()).thenReturn(portCell);
    when(pathFinder.getPath(destination, destination)).thenReturn(
      Optional.of(ImmutableList.of(destination))
    );

    assertThat(routeViaDestination.apply(vessel, destination)).isEqualTo(
      new Route(15.0, Duration.ofMinutes(90))
    );
    assertThat(routeViaDestination.apply(otherVessel, destination)).isEqualTo(
      new Route(5.0, Duration.ofMinutes(15))
    );
  }

  @Test
  void aVesselAlreadyAtTheDestinationOnlyTravelsBack() {
    when(vessel.getCell()).thenReturn(destination);
    when(pathFinder.getPath(destination, destination)).thenReturn(
      Optional.of(ImmutableList.of(destination))
    );

    final Route route = routeViaDestination.apply(vessel, destination);

    assertThat(route.getDistanceInKm()).isEqualTo(5.0);
    assertThat(route.getDuration()).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  void throwsWhenThereIsNoPath() {
    when(pathFinder.getPath(destination, portCell)).thenReturn(
      Optional.empty()
    );

    assertThatThrownBy(() ->
      routeViaDestination.apply(vessel, destination)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
