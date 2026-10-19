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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeChange;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class FleetTest {

  private static final LocalDateTime START = LocalDateTime.of(2000, 1, 1, 0, 0);

  private final TemporalSchedule schedule = new TemporalSchedule(START);
  private final SimState simState = new SimState(0, schedule) {};
  private final VesselField vesselField = mock(VesselField.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Fleet fleet = new Fleet(
    schedule,
    new SimpleEventManager(),
    vesselField,
    portGrid,
    mock(MarketGrid.class)
  );
  /** What happened, in order. */
  private final List<String> events = new ArrayList<>();
  /** Registers each item with a teardown that records its end. */
  private final VesselRegistry<String> endRecorder = (vessel, item) -> () ->
    events.add("end " + item);
  /** Built into the vessel's own store by the first build that reaches it. */
  private final VesselScopeFactory<Object> vesselMemory =
    new VesselScopeFactory<>() {
      @Override
      protected Object newInstance(final VesselScope scope) {
        scope.register(endRecorder, "vessel store");
        return new Object();
      }
    };
  private final VesselSpecification specification = new VesselSpecification(
    new SimulationScope(mock(Simulation.class)),
    fleet,
    behaviour(scope -> {
      scope.register(endRecorder, "behaviour");
      vesselMemory.get(scope);
      return new RecordingTask();
    }),
    ImmutableMap.of(
      "gear",
      componentEndingWith("gear"),
      "hold",
      componentEndingWith("hold")
    ),
    "home_port",
    null
  );

  /**
   * A behaviour that records each time it starts, then runs for a second and
   * finishes.
   */
  private class RecordingTask extends VesselTask {

    @Override
    public Status execute() {
      if (getStatus() == Status.RUNNING) {
        return Status.SUCCEEDED;
      }
      events.add("step " + getVessel().getId());
      return runFor(ofSeconds(1));
    }
  }

  private VesselComponent<Object> componentEndingWith(final String name) {
    return vesselComponent(scope -> {
      scope.register(endRecorder, name);
      return new Object();
    });
  }

  @BeforeEach
  void setUp() {
    when(portGrid.getObject("P1")).thenReturn(Optional.of(mock(Port.class)));
    doAnswer(invocation -> events.add("off the field"))
      .when(vesselField)
      .remove(any());
    fleet.addExitListener(vessel ->
      events.add(
        "exit " +
          vessel.getId() +
          (fleet.getVessel(vessel.getId()).isPresent() ? " member" : "")
      )
    );
    // Keeps the schedule from running out of events, which would stop it.
    schedule.scheduleRepeating(_ -> {}, 0, 1.0);
  }

  private Vessel create(final String id) {
    return specification.create(id, Map.of("home_port", "P1"));
  }

  private void stepFor(final Duration duration) {
    schedule.stepFor(simState, duration);
  }

  private static AttributeChange setting(
    final String attribute,
    final Object value
  ) {
    return new AttributeChange(
      ImmutableMap.of(attribute, value),
      ImmutableSet.of()
    );
  }

  @Test
  void anExitEndsTheComponentStoresInReverseThenTheVesselsStoreThenLeaves() {
    create("V1");
    stepFor(ofSeconds(1));
    events.clear();

    fleet.queue("V1", new Exit());
    stepFor(ofSeconds(1));

    assertThat(events).containsExactly(
      "end hold",
      "end gear",
      "end behaviour",
      "end vessel store",
      "off the field",
      "exit V1"
    );
    assertThat(fleet.getVessels()).isEmpty();
  }

  @Test
  void anExitedVesselHasNoComponentLeftAndIsInactive() {
    final Vessel vessel = create("V1");
    fleet.queue("V1", new Exit());
    stepFor(ofSeconds(1));

    assertThat(specification.getBehaviour().findIn(vessel)).isEmpty();
    assertThat(
      specification
        .getComponents()
        .values()
        .stream()
        .map(component -> component.findIn(vessel))
    ).allMatch(Optional::isEmpty);
    assertThat(vessel.isActive()).isFalse();
  }

  @Test
  void anExitedVesselIsNeverSteppedAgain() {
    create("V1");
    fleet.queue("V1", new Exit());
    stepFor(ofSeconds(3));

    assertThat(events).doesNotContain("step V1");
  }

  @Test
  void anExitEndsTheQueue() {
    final Vessel vessel = create("V1");
    fleet.queue("V1", new Exit());
    fleet.queue("V1", setting("gear", "PS"));
    stepFor(ofSeconds(1));

    assertThat(vessel.getAttributes()).doesNotContainKey("gear");
  }

  @Test
  void aStatementAboutAVesselThatExitedIsAnErrorNamingItsExitDate() {
    create("V1");
    stepFor(ofSeconds(5));
    fleet.queue("V1", new Exit());
    stepFor(ofSeconds(1));

    assertThatThrownBy(() -> fleet.queue("V1", setting("gear", "PS")))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining(START.plusSeconds(5).toString());
  }

  @Test
  void anExitedIdIsNeverReused() {
    create("V1");
    fleet.queue("V1", new Exit());
    stepFor(ofSeconds(1));

    assertThatThrownBy(() -> create("V1"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining(START.toString());
  }

  @Test
  void aStatementAboutAnIdTheFleetNeverSawIsAnError() {
    assertThatThrownBy(() -> fleet.queue("V9", setting("gear", "PS")))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V9");
  }

  @Test
  void anExitRecordedBeforeTheStartIsAnExitLikeAnyOther() {
    final LocalDateTime exitDate = START.minusYears(1);
    fleet.recordExit("V1", exitDate);

    assertThatThrownBy(() -> fleet.queue("V1", setting("gear", "PS")))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(exitDate.toString());
    assertThatThrownBy(() -> create("V1"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(exitDate.toString());
  }

  @Test
  void anExitCannotBeRecordedForAVesselOfTheFleet() {
    create("V1");

    assertThatThrownBy(() -> fleet.recordExit("V1", START))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1");
  }
}
