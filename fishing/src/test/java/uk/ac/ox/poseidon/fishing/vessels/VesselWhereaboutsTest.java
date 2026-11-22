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
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.varyingBy;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.when;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.HOME_PORT;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.HOME_PORT_CODE;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.specificationOf;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sim.engine.SimState;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeChange;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;
import uk.ac.ox.poseidon.fishing.vessels.Whereabouts.Absent;
import uk.ac.ox.poseidon.fishing.vessels.Whereabouts.AtSea;
import uk.ac.ox.poseidon.fishing.vessels.Whereabouts.Docked;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class VesselWhereaboutsTest {

  private static final LocalDateTime START = LocalDateTime.of(2013, 1, 1, 0, 0);
  private static final String OTHER_PORT_CODE = "P2";
  private static final Cell PORT_CELL = new Cell(0, 0);

  private final TemporalSchedule schedule = new TemporalSchedule(START);
  private final SimState simState = new SimState(0, schedule) {};
  private final VesselField vesselField = mock(VesselField.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Port homePort = mock(Port.class);
  private final Port otherPort = mock(Port.class);
  private final SimpleEventManager fleetEvents = new SimpleEventManager();
  private final Fleet fleet = new Fleet(
    schedule,
    fleetEvents,
    vesselField,
    portGrid
  );

  /** What fleet-level listeners hear, in order: events, and exits as "exit". */
  private final List<Object> heard = new ArrayList<>();

  /** Alternately starts a one-hour task and finishes it. */
  private static final class HourTask extends VesselTask {

    @Override
    public Status execute() {
      return getStatus() == Status.RUNNING
        ? Status.SUCCEEDED
        : runFor(ofHours(1));
    }
  }

  @BeforeEach
  void setUp() {
    schedule.scheduleRepeating(_ -> {}, 0, 1.0);
    when(portGrid.getObject(HOME_PORT_CODE)).thenReturn(Optional.of(homePort));
    when(portGrid.getObject(OTHER_PORT_CODE)).thenReturn(
      Optional.of(otherPort)
    );
    when(portGrid.getCell(homePort)).thenReturn(PORT_CELL);
    when(portGrid.getCell(otherPort)).thenReturn(new Cell(1, 0));
    fleetEvents.addListener(new SimpleListener<>(HasVessel.class, heard::add));
    fleet.addExitListener(_ -> heard.add("exit"));
  }

  /** @return a vessel created as a source creates one, then first-stepped */
  private Vessel created(
    final Map<String, ?> attributes,
    final VesselComponent<?>... components
  ) {
    final Vessel vessel = fleet.createVessel(
      "vessel",
      specificationOf(behaviour(_ -> new HourTask()), components)
    );
    vessel.setAttributes(attributes);
    vessel.buildComponents();
    vessel.scheduleFirstStep();
    return vessel;
  }

  private Vessel createdAtHomePort() {
    return created(Map.of(HOME_PORT, HOME_PORT_CODE));
  }

  private void step() {
    schedule.stepFor(simState, ofSeconds(1));
  }

  /** Steps an hour, past the next point the behaviour is between runs. */
  private void stepToTheNextBoundary() {
    schedule.stepFor(simState, ofHours(1));
  }

  private static AttributeChange setting(final String code) {
    return new AttributeChange(
      ImmutableMap.of(HOME_PORT, code),
      ImmutableSet.of()
    );
  }

  private static AttributeChange removingTheHomePort() {
    return new AttributeChange(ImmutableMap.of(), ImmutableSet.of(HOME_PORT));
  }

  @Test
  void aVesselWithAMatchingHomePortIsCreatedDockedThereSilently() {
    final Vessel vessel = createdAtHomePort();

    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));
    assertThat(vessel.isDocked()).isTrue();
    assertThat(vessel.getPortDockedAt()).isSameAs(homePort);
    assertThat(vessel.isDockedAt(homePort)).isTrue();
    assertThat(vessel.isDockedAt(otherPort)).isFalse();
    assertThat(heard).isEmpty();
  }

  @Test
  void aVesselWithNoMatchingHomePortIsCreatedAbsent() {
    final Vessel vessel = created(Map.of(HOME_PORT, "nowhere"));

    assertThat(vessel.getWhereabouts()).isEqualTo(new Absent());
    assertThat(vessel.isAbsent()).isTrue();
    assertThat(vessel.isDocked()).isFalse();
    assertThat(vessel.isAtSea()).isFalse();
    assertThat(vessel.isDockedAt(homePort)).isFalse();
    assertThat(vessel.isActive()).isFalse();
    assertThat(heard).isEmpty();
  }

  @Test
  void anAbsentVesselCannotBeMovedOrLocatedOrDockedOrUndocked() {
    final Vessel vessel = created(Map.of());

    assertThatThrownBy(() -> vessel.setCurrentCell(PORT_CELL)).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(vessel::getCell)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("absent");
    assertThatThrownBy(vessel::getPoint).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(vessel::getCoordinate).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(() -> vessel.dockAt(homePort, START)).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(() -> vessel.undock(START)).isInstanceOf(
      IllegalStateException.class
    );
    assertThatThrownBy(vessel::getPortDockedAt).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void undockingAndDockingMoveBetweenDockedAndAtSea() {
    final Vessel vessel = createdAtHomePort();
    when(vesselField.getCell(vessel)).thenReturn(PORT_CELL);

    vessel.undock(START);
    assertThat(vessel.getWhereabouts()).isEqualTo(new AtSea());
    assertThat(vessel.isDockedAt(homePort)).isFalse();
    assertThatThrownBy(vessel::getPortDockedAt).isInstanceOf(
      IllegalStateException.class
    );

    vessel.dockAt(homePort, START);
    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));
  }

  @Test
  void aHomePortMovingOutAndBackIsAnnouncedWithItsDateAndPort() {
    final Vessel vessel = createdAtHomePort();

    vessel.queue(removingTheHomePort());
    stepToTheNextBoundary();
    assertThat(vessel.isAbsent()).isTrue();
    assertThat(vessel.isActive()).isFalse();
    verify(vesselField).remove(vessel);

    vessel.queue(setting(HOME_PORT_CODE));
    stepToTheNextBoundary();
    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));
    assertThat(vessel.isActive()).isTrue();
    verify(vesselField, times(2)).setCell(vessel, PORT_CELL);

    assertThat(heard).containsExactly(
      new BecameAbsentEvent(vessel, homePort, START),
      new BecamePresentEvent(vessel, homePort, START.plusHours(1))
    );
  }

  @Test
  void aMoveBetweenModelledHomePortsLeavesTheVesselWhereItIsAndSilent() {
    final Vessel vessel = createdAtHomePort();

    vessel.queue(setting(OTHER_PORT_CODE));
    stepToTheNextBoundary();

    assertThat(vessel.getHomePort()).isSameAs(otherPort);
    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));
    assertThat(heard).isEmpty();
    verify(vesselField, never()).remove(vessel);
  }

  @Test
  void statementsAppliedTogetherGiveOneNetEventOrNone() {
    final Vessel vessel = createdAtHomePort();

    vessel.queue(removingTheHomePort());
    vessel.queue(setting(OTHER_PORT_CODE));
    stepToTheNextBoundary();
    assertThat(heard).isEmpty();
    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));

    vessel.queue(setting("nowhere"));
    vessel.queue(removingTheHomePort());
    stepToTheNextBoundary();
    assertThat(heard).hasSize(1).first().isInstanceOf(BecameAbsentEvent.class);
  }

  @Test
  void aVesselAtSeaCannotBecomeAbsent() {
    final Vessel vessel = createdAtHomePort();
    vessel.undock(START);

    vessel.queue(removingTheHomePort());

    assertThatThrownBy(this::stepToTheNextBoundary)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("at sea");
  }

  @Test
  void anAtSeaQueueMovingItsHomePortOutAndBackChecksOnlyTheNetResult() {
    final Vessel vessel = createdAtHomePort();
    vessel.undock(START);
    heard.clear();

    vessel.queue(removingTheHomePort());
    vessel.queue(setting(OTHER_PORT_CODE));
    stepToTheNextBoundary();

    assertThat(vessel.getWhereabouts()).isEqualTo(new AtSea());
    assertThat(vessel.getHomePort()).isSameAs(otherPort);
    assertThat(heard).isEmpty();
    verify(vesselField, never()).remove(vessel);
    verify(vesselField, never()).setCell(vessel, new Cell(1, 0));
  }

  private static Factory<
    VesselComponentScope,
    SimpleListener<HasVessel>
  > recordingEventsIn(final List<HasVessel> events) {
    return scope -> {
      final SimpleListener<HasVessel> listener = new SimpleListener<>(
        HasVessel.class,
        events::add
      );
      scope.addListener(listener);
      return listener;
    };
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void whereaboutsEventsReachTheReplacementComponentAfterRebuild(
    final boolean initiallyPresent
  ) {
    final List<HasVessel> oldEvents = new ArrayList<>();
    final List<HasVessel> newEvents = new ArrayList<>();
    final Vessel vessel = created(
      Map.of(
        HOME_PORT,
        initiallyPresent ? HOME_PORT_CODE : "nowhere",
        "generation",
        "old"
      ),
      varyingBy(
        "generation",
        when("old", recordingEventsIn(oldEvents)),
        when("new", recordingEventsIn(newEvents))
      )
    );

    vessel.queue(
      new AttributeChange(
        ImmutableMap.of(
          HOME_PORT,
          initiallyPresent ? "nowhere" : HOME_PORT_CODE,
          "generation",
          "new"
        ),
        ImmutableSet.of()
      )
    );
    stepToTheNextBoundary();

    assertThat(oldEvents).isEmpty();
    assertThat(newEvents).containsExactly(
      initiallyPresent
        ? new BecameAbsentEvent(vessel, homePort, START)
        : new BecamePresentEvent(vessel, homePort, START)
    );
  }

  @Test
  void anExitFromDockedAnnouncesBecomingAbsentBeforeTheFleetsExit() {
    final Vessel vessel = createdAtHomePort();
    final List<Vessel> absentWhileStillInFleet = new ArrayList<>();
    fleetEvents.addListener(
      new SimpleListener<>(BecameAbsentEvent.class, event -> {
        assertThat(event.getVessel()).isSameAs(vessel);
        assertThat(vessel.isAbsent()).isTrue();
        verify(vesselField).remove(vessel);
        assertThat(fleet.getVessel(vessel.getId())).contains(vessel);
        absentWhileStillInFleet.add(vessel);
      })
    );

    vessel.queue(new Exit());
    stepToTheNextBoundary();

    assertThat(heard).containsExactly(
      new BecameAbsentEvent(vessel, homePort, START),
      "exit"
    );
    assertThat(vessel.isAbsent()).isTrue();
    assertThat(absentWhileStillInFleet).containsExactly(vessel);
    assertThat(fleet.getVessel(vessel.getId())).isEmpty();
    verify(vesselField).remove(vessel);
  }

  @Test
  void anExitAtSeaIsAnError() {
    final Vessel vessel = createdAtHomePort();
    vessel.undock(START);

    vessel.queue(new Exit());

    assertThatThrownBy(this::stepToTheNextBoundary)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot exit at sea");
    assertThat(heard).isEmpty();
  }

  @Test
  void anExitWhileAbsentAnnouncesNothingButTheFleetsExit() {
    final Vessel vessel = created(Map.of());

    vessel.queue(new Exit());
    step();

    assertThat(heard).containsExactly("exit");
  }

  @Test
  void aVesselExitingAfterItsHomePortMovedOutAnnouncesOnceFromWhereItStarted() {
    final Vessel vessel = createdAtHomePort();

    vessel.queue(removingTheHomePort());
    vessel.queue(new Exit());
    stepToTheNextBoundary();

    assertThat(heard).containsExactly(
      new BecameAbsentEvent(vessel, homePort, START),
      "exit"
    );
  }
}
