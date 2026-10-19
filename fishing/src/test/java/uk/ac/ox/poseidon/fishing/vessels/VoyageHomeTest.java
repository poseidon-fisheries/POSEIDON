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
import static java.time.Duration.ofMinutes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.tasks.branches.Factories.selectorTask;
import static uk.ac.ox.poseidon.fishing.tasks.branches.Factories.sequenceTask;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeChange;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A home port changed while a vessel travels home, on a behaviour of NW Med's
 * shape: at its home port, the vessel departs on a trip; elsewhere, it travels
 * home, in a run of its own.
 */
class VoyageHomeTest {

  private final TemporalSchedule schedule = new TemporalSchedule(
    LocalDateTime.of(2000, 1, 1, 0, 0)
  );
  private final SimState simState = new SimState(0, schedule) {};
  private final VesselField vesselField = mock(VesselField.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Map<Vessel, Int2D> cells = new HashMap<>();
  private final Fleet fleet = new Fleet(
    schedule,
    new SimpleEventManager(),
    vesselField,
    portGrid,
    mock(MarketGrid.class)
  );
  /** What the vessel did, in order. */
  private final List<String> log = new ArrayList<>();
  private final VesselSpecification specification = new VesselSpecification(
    new SimulationScope(mock(Simulation.class)),
    fleet,
    behaviour(
      selectorTask(
        sequenceTask(scope -> new IsAtHomePort(), scope -> new Departure()),
        scope -> new VoyageHome()
      )
    ),
    ImmutableMap.of(),
    "home_port",
    null
  );

  private static final class IsAtHomePort extends VesselTask {

    @Override
    public Status execute() {
      return getVessel().isAtHomePort() ? Status.SUCCEEDED : Status.FAILED;
    }
  }

  /** Departs on a trip of an hour. */
  private final class Departure extends VesselTask {

    @Override
    public Status execute() {
      if (getStatus() == Status.RUNNING) return Status.SUCCEEDED;
      log.add("depart from " + getVessel().getHomePort().getCode());
      return runFor(ofHours(1));
    }
  }

  /** Travels three hours to the home port it has as it sets off. */
  private final class VoyageHome extends VesselTask {

    private Port destination;

    @Override
    public Status execute() {
      if (getStatus() == Status.RUNNING) {
        getVessel().setCurrentCell(portGrid.getLocation(destination));
        log.add("arrive at " + destination.getCode());
        return Status.SUCCEEDED;
      }
      destination = getVessel().getHomePort();
      log.add("travel home to " + destination.getCode());
      return runFor(ofHours(3));
    }
  }

  @BeforeEach
  void setUp() {
    port("P1", new Int2D(1, 1));
    port("P2", new Int2D(2, 2));
    port("P3", new Int2D(3, 3));
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

  private void port(final String code, final Int2D cell) {
    final Port port = mock(Port.class);
    when(port.getCode()).thenReturn(code);
    when(portGrid.getObject(code)).thenReturn(Optional.of(port));
    when(portGrid.getLocation(port)).thenReturn(cell);
  }

  private void stepFor(final Duration duration) {
    schedule.stepFor(simState, duration);
  }

  private void changeHomePort(final String code) {
    fleet.queue(
      "V1",
      new AttributeChange(ImmutableMap.of("home_port", code), ImmutableSet.of())
    );
  }

  @Test
  void aHomePortChangedDuringAVoyageHomeTakesEffectOnArrivalBeforeTheNextDeparture() {
    specification.create("V1", Map.of("home_port", "P1"));
    stepFor(ofMinutes(30));
    changeHomePort("P2");
    stepFor(ofMinutes(90));
    changeHomePort("P3");
    stepFor(ofHours(6));

    assertThat(log).containsExactly(
      "depart from P1",
      "travel home to P2",
      "arrive at P2",
      "travel home to P3",
      "arrive at P3",
      "depart from P3"
    );
  }
}
