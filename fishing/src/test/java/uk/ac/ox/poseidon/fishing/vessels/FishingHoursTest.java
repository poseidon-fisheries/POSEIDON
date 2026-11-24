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

package uk.ac.ox.poseidon.fishing.vessels;

import static java.time.Duration.ofHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.core.time.windows.Factories.dailyTimeWindow;
import static uk.ac.ox.poseidon.core.time.windows.Factories.durationUntilTimeWindowOpens;
import static uk.ac.ox.poseidon.core.time.windows.Factories.withinTimeWindow;
import static uk.ac.ox.poseidon.fishing.tasks.branches.Factories.fallbackTask;
import static uk.ac.ox.poseidon.fishing.tasks.branches.Factories.sequenceTask;
import static uk.ac.ox.poseidon.fishing.tasks.decorators.Factories.untilSuccess;
import static uk.ac.ox.poseidon.fishing.tasks.general.Factories.checkThat;
import static uk.ac.ox.poseidon.fishing.tasks.general.Factories.succeedOrWait;
import static uk.ac.ox.poseidon.fishing.tasks.general.Factories.waitFor;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;

import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A vessel that sets for an hour at a time and fishes only within a night
 * window (20:00 to 06:00), on a behaviour of the shape the scenarios compose:
 * before each set, it waits until the window opens, checking the window again
 * on waking, and then sets.
 */
class FishingHoursTest {

  private final TemporalSchedule schedule = new TemporalSchedule(
    LocalDateTime.of(2000, 1, 1, 12, 0)
  );
  private final SimState simState = new SimState(0, schedule) {};
  private final Simulation simulation = mock(Simulation.class);
  private final VesselField vesselField = mock(VesselField.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Map<Vessel, Cell> cells = new HashMap<>();
  private final Fleet fleet = new Fleet(
    schedule,
    new SimpleEventManager(),
    vesselField,
    portGrid
  );
  private final AtomicReference<LocalTime> opening = new AtomicReference<>(
    LocalTime.of(20, 0)
  );
  private final AtomicReference<LocalTime> closing = new AtomicReference<>(
    LocalTime.of(6, 0)
  );
  private final ValueFactory<Scope, Object, LocalTime> openingValue = scope ->
    context ->
      opening.get();
  private final ValueFactory<Scope, Object, LocalTime> closingValue = scope ->
    context ->
      closing.get();
  /** The date-times at which the vessel started a set, in order. */
  private final List<LocalDateTime> setStarts = new ArrayList<>();

  /** How many times an attempt to set failed, which a trip would abandon. */
  private int failedAttempts = 0;

  /** What the vessel does when an attempt to set fails. */
  private final class CountFailure extends VesselTask {

    @Override
    public Status execute() {
      failedAttempts++;
      return Status.SUCCEEDED;
    }
  }

  /** A set of an hour, which logs when it starts. */
  private final class FishingSet extends VesselTask {

    @Override
    public Status execute() {
      return getStatus() == Status.RUNNING ? Status.SUCCEEDED : startSet();
    }

    private Status startSet() {
      setStarts.add(schedule.getDateTime());
      return runFor(ofHours(1));
    }
  }

  private VesselSpecification specification() {
    final var nightHours = dailyTimeWindow(openingValue, closingValue);
    return new VesselSpecification(
      new SimulationScope(simulation),
      fleet,
      behaviour(
        fallbackTask(
          sequenceTask(
            untilSuccess(
              succeedOrWait(
                checkThat(currentDateTime(), withinTimeWindow(nightHours)),
                waitFor(
                  durationUntilTimeWindowOpens(currentDateTime(), nightHours)
                )
              )
            ),
            scope -> new FishingSet()
          ),
          scope -> new CountFailure()
        )
      ),
      ImmutableMap.of()
    );
  }

  @BeforeEach
  void setUp() {
    when(simulation.getTemporalSchedule()).thenReturn(schedule);
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    final Port port = mock(Port.class);
    when(port.getCode()).thenReturn("P1");
    when(portGrid.getObject("P1")).thenReturn(Optional.of(port));
    when(portGrid.getCell(port)).thenReturn(new Cell(1, 1));
    when(vesselField.setCell(any(), any())).thenAnswer(invocation -> {
      cells.put(invocation.getArgument(0), invocation.getArgument(1));
      return true;
    });
    when(vesselField.getCell(any())).thenAnswer(invocation ->
      cells.get(invocation.<Vessel>getArgument(0))
    );
    // Keeps the schedule from running out of events, which would stop it.
    schedule.scheduleRepeating(_ -> {}, 0, 1.0);
  }

  private void stepFor(final Duration duration) {
    schedule.stepFor(simState, duration);
  }

  @Test
  void aVesselOutsideTheWindowWaitsAndItsFirstSetStartsAtTheOpening() {
    specification().create("V1", Optional.of("P1"), Map.of());
    stepFor(ofHours(7));

    assertThat(setStarts).isEmpty();

    stepFor(ofHours(2));

    assertThat(setStarts)
      .first()
      .isEqualTo(LocalDateTime.of(2000, 1, 1, 20, 0));
  }

  @Test
  void everySetOverSeveralNightsStartsInsideTheWindow() {
    specification().create("V1", Optional.of("P1"), Map.of());
    stepFor(ofHours(40));

    assertThat(setStarts).hasSizeGreaterThan(10);
    assertThat(setStarts).allSatisfy(start ->
      assertThat(
        !start.toLocalTime().isBefore(LocalTime.of(20, 0)) ||
          start.toLocalTime().isBefore(LocalTime.of(6, 0))
      ).isTrue()
    );
  }

  @Test
  void anOpeningMovedLaterDuringAWaitIsWaitedForAgain() {
    specification().create("V1", Optional.of("P1"), Map.of());
    stepFor(ofHours(3));
    opening.set(LocalTime.of(22, 0));
    stepFor(ofHours(9));

    assertThat(setStarts)
      .first()
      .isEqualTo(LocalDateTime.of(2000, 1, 1, 22, 0));
    assertThat(failedAttempts).isZero();
  }
}
