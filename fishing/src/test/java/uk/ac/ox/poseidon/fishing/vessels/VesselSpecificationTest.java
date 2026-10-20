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

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class VesselSpecificationTest {

  private static final Int2D PORT_CELL = new Int2D(1, 2);

  private final TemporalSchedule schedule = new TemporalSchedule(
    LocalDateTime.of(2000, 1, 1, 0, 0)
  );
  private final SimState simState = new SimState(0, schedule) {};
  private final VesselField vesselField = mock(VesselField.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Port port = mock(Port.class);
  private final Fleet fleet = new Fleet(
    schedule,
    new SimpleEventManager(),
    vesselField,
    portGrid,
    mock(MarketGrid.class)
  );
  /** What happened, in order: builds, entries and steps, by vessel id. */
  private final List<String> events = new ArrayList<>();
  private final VesselSpecification specification = new VesselSpecification(
    new SimulationScope(mock(Simulation.class)),
    fleet,
    behaviour(scope -> {
      events.add("behaviour " + scope.getVessel().getId());
      return new RecordingTask();
    }),
    ImmutableMap.of(
      "gear",
      recordedComponent("gear"),
      "hold",
      recordedComponent("hold")
    ),
    "home_port",
    "name"
  );

  /**
   * A behaviour that records each time it starts, then runs for a second and
   * finishes.
   */
  private final class RecordingTask extends VesselTask {

    @Override
    public Status execute() {
      if (getStatus() == Status.RUNNING) {
        return Status.SUCCEEDED;
      }
      events.add("step " + getVessel().getId());
      return runFor(ofSeconds(1));
    }
  }

  private VesselComponent<Object> recordedComponent(final String name) {
    return vesselComponent(scope -> {
      events.add(name + " " + scope.getVessel().getId());
      return new Object();
    });
  }

  @BeforeEach
  void setUp() {
    when(portGrid.getObject("P1")).thenReturn(Optional.of(port));
    when(portGrid.getCell(port)).thenReturn(PORT_CELL);
    fleet.addEntryListener(vessel -> events.add("entry " + vessel.getId()));
    // Keeps the schedule from running out of events, which would stop it.
    schedule.scheduleRepeating(_ -> {}, 0, 1.0);
  }

  private Vessel create(final String id) {
    return specification.create(id, Map.of("home_port", "P1"));
  }

  @Test
  void theFleetAnnouncesEachCompleteVesselInOrderBeforeItsFirstStep() {
    create("V1");
    create("V2");

    assertThat(events).containsExactly(
      "behaviour V1",
      "gear V1",
      "hold V1",
      "entry V1",
      "behaviour V2",
      "gear V2",
      "hold V2",
      "entry V2"
    );

    schedule.stepFor(simState, ofSeconds(2));
    assertThat(events.subList(8, events.size())).contains("step V1", "step V2");
  }

  @Test
  void aBuildSeesTheVesselsAttributesHomePortAndAccount() {
    final List<Vessel> vesselsBuilt = new ArrayList<>();
    final VesselSpecification specification = new VesselSpecification(
      new SimulationScope(mock(Simulation.class)),
      fleet,
      behaviour(scope -> {
        final Vessel vessel = scope.getVessel();
        assertThat(vessel.getAttributes()).containsEntry("home_port", "P1");
        assertThat(vessel.getHomePort()).isSameAs(port);
        assertThat(vessel.getName()).contains("Vee One");
        assertThat(vessel.getAccount()).isNotNull();
        vesselsBuilt.add(vessel);
        return new RecordingTask();
      }),
      ImmutableMap.of(),
      "home_port",
      "name"
    );

    final Vessel vessel = specification.create(
      "V1",
      Map.of("home_port", "P1", "name", "Vee One")
    );

    assertThat(vesselsBuilt).containsExactly(vessel);
  }

  @Test
  void aNewVesselIsPlacedAtItsHomePort() {
    final Vessel vessel = create("V1");

    verify(vesselField).setCell(vessel, PORT_CELL);
  }

  @Test
  void aVesselIsKnownByItsNameAndIdOrByItsIdAlone() {
    final Vessel named = specification.create(
      "V1",
      Map.of("home_port", "P1", "name", "Vee One")
    );
    final Vessel unnamed = create("V2");

    assertThat(named).hasToString("Vee One (V1)");
    assertThat(unnamed.getName()).isEmpty();
    assertThat(unnamed).hasToString("V2");
  }

  @Test
  void aHomePortCodeNamingNoPortGivesNoHomePortAndAnInactiveVessel() {
    when(portGrid.getObject("P9")).thenReturn(Optional.empty());

    final Vessel vessel = specification.create("V1", Map.of("home_port", "P9"));

    assertThat(vessel.getHomePort()).isNull();
    assertThat(vessel.isActive()).isFalse();
  }

  @Test
  void anIdTheFleetAlreadyHasIsRefused() {
    create("V1");

    assertThatThrownBy(() -> create("V1"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1");
  }

  @Test
  void eachVesselHasItsOwnEventManager() {
    final Vessel first = create("V1");
    final Vessel second = create("V2");
    final List<String> receivedEvents = new ArrayList<>();
    first
      .getEventManager()
      .addListener(new SimpleListener<>(String.class, receivedEvents::add));

    second.getEventManager().broadcast("second");
    assertThat(receivedEvents).isEmpty();

    first.getEventManager().broadcast("first");
    assertThat(receivedEvents).containsExactly("first");
  }
}
