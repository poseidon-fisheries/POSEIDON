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

class TravelTimeToCellTest {

  private final Cell vesselCell = new Cell(0, 0);
  private final Cell waypoint = new Cell(1, 0);
  private final Cell destination = new Cell(1, 1);
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
  private final TravelTimeToCell travelTimeToCell = new TravelTimeToCell(
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
    when(
      distanceCalculator.distanceInKm(any(Cell.class), any(Cell.class))
    ).thenReturn(5.0);
    when(distanceCalculator.distanceInKm(anyList())).thenCallRealMethod();
    when(pathFinder.getPath(vesselCell, destination)).thenReturn(
      Optional.of(ImmutableList.of(vesselCell, waypoint, destination))
    );
  }

  @Test
  void takesTheTimeToTravelThePathAtTheCruisingSpeed() {
    assertThat(travelTimeToCell.apply(vessel, destination)).isEqualTo(
      Duration.ofMinutes(60)
    );
  }

  @Test
  void readsTheVesselsCurrentCellAndSpeedAtEachCall() {
    final Duration before = travelTimeToCell.apply(vessel, destination);

    when(vessel.getCell()).thenReturn(waypoint);
    when(engine.getCruisingSpeedInKph()).thenReturn(20.0);
    when(pathFinder.getPath(waypoint, destination)).thenReturn(
      Optional.of(ImmutableList.of(waypoint, destination))
    );
    final Duration after = travelTimeToCell.apply(vessel, destination);

    assertThat(before).isEqualTo(Duration.ofMinutes(60));
    assertThat(after).isEqualTo(Duration.ofMinutes(15));
  }

  @Test
  void givesTheTimeOfTheVesselGiven() {
    final Vessel otherVessel = mock(Vessel.class);
    final Engine otherEngine = mock(Engine.class);
    when(otherEngine.getCruisingSpeedInKph()).thenReturn(20.0);
    giveComponent(otherVessel, engineComponent, otherEngine);
    when(otherVessel.getCell()).thenReturn(vesselCell);

    assertThat(travelTimeToCell.apply(vessel, destination)).isEqualTo(
      Duration.ofMinutes(60)
    );
    assertThat(travelTimeToCell.apply(otherVessel, destination)).isEqualTo(
      Duration.ofMinutes(30)
    );
  }

  @Test
  void aVesselAlreadyAtTheCellTakesNoTime() {
    when(vessel.getCell()).thenReturn(destination);
    when(pathFinder.getPath(destination, destination)).thenReturn(
      Optional.of(ImmutableList.of(destination))
    );

    assertThat(travelTimeToCell.apply(vessel, destination)).isEqualTo(
      Duration.ZERO
    );
  }

  @Test
  void throwsWhenThereIsNoPath() {
    when(pathFinder.getPath(vesselCell, destination)).thenReturn(
      Optional.empty()
    );

    assertThatThrownBy(() ->
      travelTimeToCell.apply(vessel, destination)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
