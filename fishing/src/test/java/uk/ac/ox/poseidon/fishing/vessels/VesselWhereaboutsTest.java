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
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.varyingBy;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.HOME_PORT_CODE;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.specificationOf;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeUpdate;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.HomePortUpdate;
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
  private Vessel createVessel(
    final Optional<String> homePortCode,
    final Map<String, ?> attributes,
    final VesselComponent<?>... components
  ) {
    return createVessel(
      behaviour(_ -> new HourTask()),
      homePortCode,
      attributes,
      components
    );
  }

  /** @return a vessel with {@code behaviour}, created as a source does */
  private Vessel createVessel(
    final VesselComponent<? extends Task<Vessel>> behaviour,
    final Optional<String> homePortCode,
    final Map<String, ?> attributes,
    final VesselComponent<?>... components
  ) {
    final Vessel vessel = fleet.createVessel(
      "vessel",
      specificationOf(behaviour, components)
    );
    vessel.setStartingState(homePortCode, attributes);
    vessel.buildComponents();
    vessel.scheduleFirstStep();
    return vessel;
  }

  private Vessel createdAtHomePort() {
    return createVessel(Optional.of(HOME_PORT_CODE), Map.of());
  }

  private void step() {
    schedule.stepFor(simState, ofSeconds(1));
  }

  /** Steps an hour, past the next point the behaviour is between runs. */
  private void stepToTheNextBoundary() {
    schedule.stepFor(simState, ofHours(1));
  }

  private static HomePortUpdate setting(final String code) {
    return new HomePortUpdate(Optional.of(code));
  }

  private static HomePortUpdate removingTheHomePort() {
    return new HomePortUpdate(Optional.empty());
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
    final Vessel vessel = createVessel(Optional.of("nowhere"), Map.of());

    assertThat(vessel.getWhereabouts()).isEqualTo(new Absent());
    assertThat(vessel.isPresent()).isFalse();
    assertThat(vessel.isDocked()).isFalse();
    assertThat(vessel.isAtSea()).isFalse();
    assertThat(vessel.isDockedAt(homePort)).isFalse();
    assertThat(heard).isEmpty();
  }

  @Test
  void anAbsentVesselCannotBeMovedOrLocatedOrDockedOrUndocked() {
    final Vessel vessel = createVessel(Optional.empty(), Map.of());

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
    assertThat(vessel.isPresent()).isFalse();
    verify(vesselField).remove(vessel);

    vessel.queue(setting(HOME_PORT_CODE));
    stepToTheNextBoundary();
    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));
    assertThat(vessel.isPresent()).isTrue();
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

    assertThat(vessel.getHomePort()).containsSame(otherPort);
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
    assertThat(vessel.getHomePort()).containsSame(otherPort);
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

  @Test
  void anUpdateEqualToTheCurrentStateRebuildsNothing() {
    final List<Object> builtComponents = new ArrayList<>();
    final Factory<VesselComponentScope, Object> build = _ ->
      builtComponents.add(new Object());
    final Vessel vessel = createVessel(
      Optional.of(HOME_PORT_CODE),
      Map.of("generation", "old"),
      varyingBy("generation", entry("old", build), entry("new", build))
    );
    assertThat(builtComponents).hasSize(1);

    vessel.queue(setting(HOME_PORT_CODE));
    vessel.queue(
      new AttributeUpdate(
        ImmutableMap.of("generation", "old"),
        ImmutableSet.of()
      )
    );
    stepToTheNextBoundary();

    assertThat(builtComponents).hasSize(1);
    assertThat(vessel.getHomePort()).containsSame(homePort);
  }

  /**
   * @return a vessel created at {@code homePortCode}, with a component varying
   *         by generation, recording in {@code oldEvents} and
   *         {@code newEvents} what each generation's build hears, then given
   *         the home port {@code newHomePortCode} and the new generation
   */
  private Vessel movedFromTo(
    final String homePortCode,
    final String newHomePortCode,
    final List<HasVessel> oldEvents,
    final List<HasVessel> newEvents
  ) {
    final Vessel vessel = createVessel(
      Optional.of(homePortCode),
      Map.of("generation", "old"),
      varyingBy(
        "generation",
        entry("old", recordingEventsIn(oldEvents)),
        entry("new", recordingEventsIn(newEvents))
      )
    );
    vessel.queue(setting(newHomePortCode));
    vessel.queue(
      new AttributeUpdate(
        ImmutableMap.of("generation", "new"),
        ImmutableSet.of()
      )
    );
    stepToTheNextBoundary();
    return vessel;
  }

  @Test
  void aComponentHearsItsVesselBecomePresent() {
    final List<HasVessel> oldEvents = new ArrayList<>();
    final List<HasVessel> newEvents = new ArrayList<>();

    final Vessel vessel = movedFromTo(
      "nowhere",
      HOME_PORT_CODE,
      oldEvents,
      newEvents
    );

    assertThat(oldEvents).isEmpty();
    assertThat(newEvents).containsExactly(
      new BecamePresentEvent(vessel, homePort, START)
    );
  }

  @Test
  void noComponentHearsItsVesselBecomeAbsent() {
    final List<HasVessel> oldEvents = new ArrayList<>();
    final List<HasVessel> newEvents = new ArrayList<>();

    movedFromTo(HOME_PORT_CODE, "nowhere", oldEvents, newEvents);

    assertThat(oldEvents).isEmpty();
    assertThat(newEvents).isEmpty();
  }

  /** @return a behaviour listed for the gears PS and OTB only */
  private static VesselComponent<Task<Vessel>> behaviourForPsAndOtb() {
    final Factory<VesselComponentScope, Task<Vessel>> hourTask = _ ->
      new HourTask();
    return varyingBy("gear", entry("PS", hourTask), entry("OTB", hourTask));
  }

  /** @return a component recording in {@code log} its builds and its ends */
  private static Factory<VesselComponentScope, Object> recordingBuildsIn(
    final List<String> log
  ) {
    return scope -> {
      log.add("build");
      scope.addTeardown(_ -> log.add("end"));
      return new Object();
    };
  }

  private static AttributeUpdate settingGear(final String gear) {
    return new AttributeUpdate(
      ImmutableMap.of("gear", gear),
      ImmutableSet.of()
    );
  }

  @Test
  void aVesselWhoseBehaviourIsNotListedIsCreatedAbsentAndBuildsNothing() {
    final List<String> log = new ArrayList<>();
    final Vessel vessel = createVessel(
      behaviourForPsAndOtb(),
      Optional.of(HOME_PORT_CODE),
      Map.of("gear", "GNS"),
      vesselComponent(recordingBuildsIn(log))
    );

    assertThat(vessel.getWhereabouts()).isEqualTo(new Absent());
    assertThat(vessel.isPresent()).isFalse();
    assertThat(log).isEmpty();
    assertThat(heard).isEmpty();
  }

  @Test
  void aDockedVesselWhoseBehaviourStopsResolvingDropsItsComponentsAndBecomesAbsent() {
    final List<String> log = new ArrayList<>();
    final Vessel vessel = createVessel(
      behaviourForPsAndOtb(),
      Optional.of(HOME_PORT_CODE),
      Map.of("gear", "PS"),
      vesselComponent(recordingBuildsIn(log))
    );

    vessel.queue(settingGear("GNS"));
    stepToTheNextBoundary();

    assertThat(vessel.getWhereabouts()).isEqualTo(new Absent());
    assertThat(vessel.isPresent()).isFalse();
    assertThat(log).containsExactly("build", "end");
    assertThat(heard).containsExactly(
      new BecameAbsentEvent(vessel, homePort, START)
    );
    verify(vesselField).remove(vessel);
  }

  @Test
  void anAbsentVesselWhoseBehaviourResolvesAgainBuildsItsComponentsAndDocks() {
    final List<String> log = new ArrayList<>();
    final Vessel vessel = createVessel(
      behaviourForPsAndOtb(),
      Optional.of(HOME_PORT_CODE),
      Map.of("gear", "GNS"),
      vesselComponent(recordingBuildsIn(log))
    );

    vessel.queue(settingGear("PS"));
    stepToTheNextBoundary();

    assertThat(vessel.getWhereabouts()).isEqualTo(new Docked(homePort));
    assertThat(vessel.isPresent()).isTrue();
    assertThat(log).containsExactly("build");
    assertThat(heard).containsExactly(
      new BecamePresentEvent(vessel, homePort, START)
    );
  }

  @Test
  void anAbsentVesselBuildsNothingWhateverItsAttributesDo() {
    final List<String> log = new ArrayList<>();
    final Vessel vessel = createVessel(
      behaviourForPsAndOtb(),
      Optional.of("nowhere"),
      Map.of("gear", "PS"),
      varyingBy(
        "gear",
        entry("PS", recordingBuildsIn(log)),
        entry("OTB", recordingBuildsIn(log))
      )
    );

    vessel.queue(settingGear("OTB"));
    step();

    assertThat(vessel.getWhereabouts()).isEqualTo(new Absent());
    assertThat(log).isEmpty();
    assertThat(heard).isEmpty();
  }

  @Test
  void anExitFromDockedAnnouncesBecomingAbsentBeforeTheFleetsExit() {
    final Vessel vessel = createdAtHomePort();
    final List<Vessel> absentWhileStillInFleet = new ArrayList<>();
    fleetEvents.addListener(
      new SimpleListener<>(BecameAbsentEvent.class, event -> {
        assertThat(event.getVessel()).isSameAs(vessel);
        assertThat(vessel.isPresent()).isFalse();
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
    assertThat(vessel.isPresent()).isFalse();
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
    final Vessel vessel = createVessel(Optional.empty(), Map.of());

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
