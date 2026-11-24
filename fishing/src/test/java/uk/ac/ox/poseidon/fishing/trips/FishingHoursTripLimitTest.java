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

package uk.ac.ox.poseidon.fishing.trips;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.functions.Factories.combinedDuration;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dateTimeAfter;
import static uk.ac.ox.poseidon.core.time.Factories.duration;
import static uk.ac.ox.poseidon.core.time.Factories.time;
import static uk.ac.ox.poseidon.core.time.windows.Factories.dailyTimeWindow;
import static uk.ac.ox.poseidon.core.time.windows.Factories.durationUntilTimeWindowOpens;
import static uk.ac.ox.poseidon.fishing.travel.Factories.travelTimeHomeFromCell;
import static uk.ac.ox.poseidon.fishing.travel.Factories.travelTimeToCell;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentGiving;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;

/**
 * The time a trip to a candidate cell would take, as a scenario composes it
 * from travel times, the wait for fishing hours and the set: a vessel at its
 * home port, a candidate two hours away and a set of an hour, with fishing
 * hours from 20:00 to 06:00.
 */
class FishingHoursTripLimitTest {

  private static final Cell PORT = new Cell(0, 0);
  private static final Cell CANDIDATE = new Cell(2, 0);

  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);
  private final Vessel vessel = mock(Vessel.class);
  private final VesselComponent<Engine> engine = componentGiving(
    vessel,
    engineGoing10Kph()
  );
  private final Factory<SimulationScope, PathLengthCalculator> pathLengths =
    scope -> pathLengthCalculator();

  private static Engine engineGoing10Kph() {
    final Engine engine = mock(Engine.class);
    when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
    return engine;
  }

  private static PathLengthCalculator pathLengthCalculator() {
    final GridPathFinder pathFinder = mock(GridPathFinder.class);
    final DistanceCalculator distance = mock(DistanceCalculator.class);
    final ModelGrid grid = ModelGrid.create(3, 1, new Envelope(10, 13, 40, 41));
    final ImmutableList<Cell> out = ImmutableList.of(
      PORT,
      new Cell(1, 0),
      CANDIDATE
    );
    final ImmutableList<Cell> home = out.reverse();
    when(pathFinder.getModelGrid()).thenReturn(grid);
    when(pathFinder.getPath(PORT, CANDIDATE)).thenReturn(Optional.of(out));
    when(pathFinder.getPath(CANDIDATE, PORT)).thenReturn(Optional.of(home));
    when(distance.distanceInKm(out)).thenReturn(20.0);
    when(distance.distanceInKm(home)).thenReturn(20.0);
    return new PathLengthCalculator(pathFinder, distance);
  }

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(vessel.getCell()).thenReturn(PORT);
    when(vessel.getHomePortCell()).thenReturn(Optional.of(PORT));
  }

  private void clockAt(final int hour) {
    when(simulation.getTemporalSchedule()).thenReturn(
      new TemporalSchedule(LocalDateTime.of(2000, 1, 1, hour, 0))
    );
  }

  /** Travel out, wait from the arrival for the hours, set, travel home. */
  private Duration tripDurationWithFishingHours() {
    final var arrival = dateTimeAfter(
      currentDateTime(),
      travelTimeToCell(engine, pathLengths)
    );
    return combinedDuration(
      travelTimeToCell(engine, pathLengths),
      durationUntilTimeWindowOpens(
        arrival,
        dailyTimeWindow(time(20, 0, 0), time(6, 0, 0))
      ),
      always(duration(0, 1, 0, 0)),
      travelTimeHomeFromCell(engine, pathLengths)
    )
      .get(scope)
      .apply(vessel, CANDIDATE);
  }

  @Test
  void theWaitIsCountedFromTheArrivalNotTheDeparture() {
    // Leaves at 05:00 and arrives at 07:00, so it waits thirteen hours for
    // 20:00, not eleven
    clockAt(5);

    assertThat(tripDurationWithFishingHours()).isEqualTo(Duration.ofHours(18));
  }

  @Test
  void aVesselArrivingInsideTheWindowDoesNotWait() {
    clockAt(18);

    assertThat(tripDurationWithFishingHours()).isEqualTo(Duration.ofHours(5));
  }

  @Test
  void aFleetFishingAroundTheClockHasNoWaitTerm() {
    clockAt(5);

    assertThat(
      combinedDuration(
        travelTimeToCell(engine, pathLengths),
        always(duration(0, 1, 0, 0)),
        travelTimeHomeFromCell(engine, pathLengths)
      )
        .get(scope)
        .apply(vessel, CANDIDATE)
    ).isEqualTo(Duration.ofHours(5));
  }
}
