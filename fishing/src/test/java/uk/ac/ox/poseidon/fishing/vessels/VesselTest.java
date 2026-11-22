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
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.HOME_PORT_CODE;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.newVessel;

import com.badlogic.gdx.ai.btree.Task;
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
import sim.util.Double2D;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.Sale;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.regulations.actions.InstantFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingActionEvent;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeUpdate;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.HomePortUpdate;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class VesselTest {

  private static final LocalDateTime START = LocalDateTime.of(2013, 1, 1, 0, 0);

  private final TemporalSchedule schedule = new TemporalSchedule(START);
  private final SimState simState = new SimState(0, schedule) {};
  private final RecordingTask behaviour = new RecordingTask();
  private final Vessel vessel = vesselWithItsBehaviour(behaviour);

  /**
   * A behaviour that records when it is run. Each run alternates between
   * starting a task of {@link #taskDuration} and finishing it.
   */
  private final class RecordingTask extends VesselTask {

    private final List<LocalDateTime> stepDateTimes = new ArrayList<>();
    private final List<Vessel> vesselsRead = new ArrayList<>();
    private Duration taskDuration = ofSeconds(1);

    @Override
    public Status execute() {
      stepDateTimes.add(schedule.getDateTime());
      vesselsRead.add(getVessel());
      return getStatus() == Status.RUNNING
        ? Status.SUCCEEDED
        : runFor(taskDuration);
    }
  }

  /**
   * @return a vessel on {@link #schedule} with a home port and {@code
   *         behaviour} built, whose first step is not yet scheduled
   */
  private Vessel vesselWithItsBehaviour(final Task<Vessel> behaviour) {
    final VesselComponent<Task<Vessel>> behaviourComponent = behaviour(
      scope -> behaviour
    );
    final Vessel vessel = vesselWithAHomePort(behaviourComponent);
    vessel.buildComponents();
    return vessel;
  }

  /**
   * @return a vessel on {@link #schedule} with a home port, which steps the
   *         behaviour {@code behaviour} builds, with no component built
   */
  private Vessel vesselWithAHomePort(
    final VesselComponent<? extends Task<Vessel>> behaviour
  ) {
    final PortGrid portGrid = mock(PortGrid.class);
    when(portGrid.getObject("P1")).thenReturn(Optional.of(mock(Port.class)));
    final Vessel vessel = newVessel(
      schedule,
      mock(VesselField.class),
      portGrid,
      behaviour
    );
    vessel.setStartingState(Optional.of("P1"), Map.of());
    return vessel;
  }

  /**
   * Keeps the schedule from running out of events, which would stop it.
   */
  @BeforeEach
  void setUp() {
    schedule.scheduleRepeating(_ -> {}, 0, 1.0);
  }

  private static AttributeUpdate setting(
    final String attribute,
    final Object value
  ) {
    return new AttributeUpdate(
      ImmutableMap.of(attribute, value),
      ImmutableSet.of()
    );
  }

  private static AttributeUpdate removing(final String attribute) {
    return new AttributeUpdate(ImmutableMap.of(), ImmutableSet.of(attribute));
  }

  private void stepFor(final Duration duration) {
    schedule.stepFor(simState, duration);
  }

  @Test
  void aVesselWithItsBehaviourAndAHomePortIsActive() {
    assertThat(vessel.isActive()).isTrue();
  }

  @Test
  void aVesselWithoutAHomePortIsInactive() {
    vessel.setStartingState(Optional.empty(), Map.of());
    assertThat(vessel.isActive()).isFalse();
  }

  @Test
  void aVesselWhoseBehaviourIsNotBuiltIsInactive() {
    final Vessel vessel = vesselWithAHomePort(behaviour(scope -> behaviour));
    assertThat(vessel.isActive()).isFalse();
  }

  @Test
  void aVesselIsNotSteppedBeforeItsFirstStepIsScheduled() {
    stepFor(ofSeconds(3));
    assertThat(behaviour.stepDateTimes).isEmpty();
  }

  @Test
  void theBehaviourReachesItsVesselOnlyDuringAStep() {
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(1));
    assertThat(behaviour.vesselsRead).containsExactly(vessel);
    assertThatThrownBy(behaviour::getVessel)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("read its vessel outside a step");
  }

  @Test
  void aBehaviourThatFinishesItsTaskRestartsInTheSameStep() {
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(3));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START,
      START.plusSeconds(1),
      START.plusSeconds(1),
      START.plusSeconds(2),
      START.plusSeconds(2)
    );
  }

  @Test
  void aBehaviourThatFinishesWithoutStartingATaskFails() {
    final class FinishesAtOnceTask extends VesselTask {

      @Override
      public Status execute() {
        return Status.SUCCEEDED;
      }
    }
    vesselWithItsBehaviour(new FinishesAtOnceTask()).scheduleFirstStep();
    assertThatThrownBy(() -> stepFor(ofSeconds(1)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("finished without starting a task");
  }

  @Test
  void aStatementOnAVesselWithNoStepPendingSchedulesOneAtOnce() {
    vessel.setStartingState(Optional.empty(), Map.of());
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(2));
    vessel.queue(new HomePortUpdate(Optional.of("P1")));
    assertThat(vessel.getHomePort()).isEmpty();
    stepFor(ofSeconds(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(2),
      START.plusSeconds(3),
      START.plusSeconds(3)
    );
  }

  @Test
  void aStatementOnAVesselWithAStepPendingKeepsASingleStepChain() {
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(2));
    vessel.queue(setting("gear", "PS"));
    vessel.queue(setting("gear", "OTB"));
    stepFor(ofSeconds(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START,
      START.plusSeconds(1),
      START.plusSeconds(1),
      START.plusSeconds(2),
      START.plusSeconds(2),
      START.plusSeconds(3),
      START.plusSeconds(3)
    );
    assertThat(vessel.getAttributes()).containsEntry("gear", "OTB");
  }

  @Test
  void aVesselThatLosesItsHomePortStopsUntilAStatementGivesItOneBack() {
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(2));
    vessel.queue(new HomePortUpdate(Optional.empty()));
    stepFor(ofSeconds(3));
    vessel.queue(new HomePortUpdate(Optional.of("P1")));
    stepFor(ofSeconds(2));
    // The task running when the home port goes is finished at 2 seconds
    assertThat(behaviour.stepDateTimes).containsExactly(
      START,
      START.plusSeconds(1),
      START.plusSeconds(1),
      START.plusSeconds(2),
      START.plusSeconds(5),
      START.plusSeconds(6),
      START.plusSeconds(6)
    );
  }

  @Test
  void aStatementMadeDuringTheVesselsOwnStepIsAppliedWhenItsTaskFinishes() {
    final class StatesItsGearTask extends VesselTask {

      @Override
      public Status execute() {
        return getStatus() == Status.RUNNING ? Status.SUCCEEDED : stateGear();
      }

      private Status stateGear() {
        getVessel().queue(setting("gear", "PS"));
        return runFor(ofSeconds(1));
      }
    }
    final Vessel vessel = vesselWithItsBehaviour(new StatesItsGearTask());
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(1));
    assertThat(vessel.getAttributes()).doesNotContainKey("gear");
    stepFor(ofSeconds(1));
    assertThat(vessel.getAttributes()).containsEntry("gear", "PS");
  }

  @Test
  void aRunningBehaviourIsSteppedAgainAfterItsTaskDuration() {
    behaviour.taskDuration = ofHours(1);
    vessel.scheduleFirstStep();
    stepFor(ofHours(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START,
      START.plusHours(1),
      START.plusHours(1)
    );
  }

  @Test
  void aBehaviourThatKeepsRunningWithoutSettingANewTaskDurationFails() {
    final class KeepsRunningTask extends VesselTask {

      @Override
      public Status execute() {
        return getStatus() == Status.RUNNING
          ? Status.RUNNING
          : runFor(ofHours(1));
      }
    }
    vesselWithItsBehaviour(new KeepsRunningTask()).scheduleFirstStep();
    stepFor(ofSeconds(1));
    assertThatThrownBy(() -> stepFor(ofHours(2)))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("no task set a duration this step");
  }

  @Test
  void twoTasksCannotSetADurationInOneStep() {
    final class SetsTwoDurationsTask extends VesselTask {

      @Override
      public Status execute() {
        getVessel().setTaskDuration(ofHours(1));
        return runFor(ofHours(2));
      }
    }
    vesselWithItsBehaviour(new SetsTwoDurationsTask()).scheduleFirstStep();
    assertThatThrownBy(() -> stepFor(ofSeconds(1)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("task duration was already set this step");
  }

  @Test
  void aBehaviourThatFinishesWithATaskDurationSetFails() {
    final class FinishesWithADurationTask extends VesselTask {

      @Override
      public Status execute() {
        getVessel().setTaskDuration(ofHours(1));
        return Status.SUCCEEDED;
      }
    }
    vesselWithItsBehaviour(new FinishesWithADurationTask()).scheduleFirstStep();
    assertThatThrownBy(() -> stepFor(ofSeconds(1)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("not running, but a task set a duration");
  }

  @Test
  void aStatementWaitsUntilTheBehaviourFinishesItsTask() {
    behaviour.taskDuration = ofHours(1);
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(1));
    vessel.queue(setting("gear", "PS"));
    // Up to just before the step at one hour, in which the task finishes
    stepFor(ofHours(1).minusSeconds(1));
    assertThat(vessel.getAttributes()).doesNotContainKey("gear");
    stepFor(ofSeconds(1));
    assertThat(vessel.getAttributes()).containsEntry("gear", "PS");
  }

  /** The cell {@link #vesselDockedAt} places its vessel and port on. */
  private static final Cell PORT_CELL = new Cell(0, 0);

  /**
   * @return a vessel whose home port is {@code port}, docked there on
   *         {@link #PORT_CELL}, on no trip
   */
  private static Vessel vesselDockedAt(final Port port) {
    final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
    final PortGrid portGrid = mock(PortGrid.class);
    final Vessel vessel = newVessel(
      mock(TemporalSchedule.class),
      vesselField,
      portGrid,
      behaviour(_ -> mock(VesselTask.class))
    );
    when(vesselField.getCell(vessel)).thenReturn(PORT_CELL);
    when(portGrid.getObject(HOME_PORT_CODE)).thenReturn(Optional.of(port));
    when(portGrid.getCell(port)).thenReturn(PORT_CELL);
    vessel.setStartingState(Optional.of(HOME_PORT_CODE), Map.of());
    return vessel;
  }

  @Test
  void aVesselStartsDockedAtItsHomePort() {
    final Port homePort = mock(Port.class);
    final PortGrid portGrid = mock(PortGrid.class);
    when(portGrid.getObject("P1")).thenReturn(Optional.of(homePort));
    when(portGrid.getCell(homePort)).thenReturn(PORT_CELL);
    final Vessel vessel = newVessel(
      schedule,
      mock(VesselField.class),
      portGrid,
      behaviour(_ -> behaviour)
    );

    vessel.setStartingState(Optional.of("P1"), Map.of());

    assertThat(vessel.getPortDockedAt()).isSameAs(homePort);
  }

  @Test
  void aDockedVesselCannotMove() {
    final Vessel vessel = vesselDockedAt(mock(Port.class));

    assertThatThrownBy(() -> vessel.setCurrentCell(new Cell(1, 0)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot move while");
  }

  @Test
  void aVesselDocksOnlyInThePortsCell() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);
    vessel.undock(START);
    final Port elsewhere = mock(Port.class);
    when(vessel.getPortGrid().getCell(elsewhere)).thenReturn(new Cell(1, 0));

    assertThatThrownBy(() -> vessel.dockAt(elsewhere, START))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("not the port's cell");
    assertThat(vessel.isDocked()).isFalse();
  }

  @Test
  void aDockedVesselCannotDockAgain() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);

    assertThatThrownBy(() -> vessel.dockAt(port, START))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot dock at");
  }

  @Test
  void aVesselAtSeaCannotUndock() {
    final Vessel vessel = vesselDockedAt(mock(Port.class));
    vessel.undock(START);

    assertThatThrownBy(() -> vessel.undock(START))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("not docked");
  }

  @Test
  void dockingAndUndockingAreBroadcastOnTheVesselsEventManager() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);
    final List<Object> events = new ArrayList<>();
    vessel
      .getEventManager()
      .addListener(new SimpleListener<>(DockingEvent.class, events::add));
    vessel
      .getEventManager()
      .addListener(new SimpleListener<>(UndockingEvent.class, events::add));

    vessel.undock(START.plusHours(1));
    vessel.dockAt(port, START.plusHours(2));

    assertThat(events).containsExactly(
      new UndockingEvent(vessel, port, START.plusHours(1)),
      new DockingEvent(vessel, port, START.plusHours(2))
    );
  }

  @Test
  void startingATripUndocksTheVesselFromTheTripsOrigin() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);

    startTrip(vessel);

    assertThat(vessel.isDocked()).isFalse();
    assertThat(vessel.getCurrentTrip().getOrigin()).isSameAs(port);
  }

  @Test
  void aVesselAtSeaCannotStartATrip() {
    final Vessel vessel = vesselDockedAt(mock(Port.class));
    vessel.undock(START);

    assertThatThrownBy(() -> startTrip(vessel))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot start a trip at sea");
  }

  @Test
  void aDockingDuringATripReachesTheTripsListeners() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);
    startTrip(vessel);
    final List<DockingEvent> events = new ArrayList<>();
    vessel
      .getCurrentTrip()
      .getEventManager()
      .addListener(new SimpleListener<>(DockingEvent.class, events::add));

    vessel.dockAt(port, START.plusHours(8));

    assertThat(events).containsExactly(
      new DockingEvent(vessel, port, START.plusHours(8))
    );
  }

  @Test
  void aStopDuringATripBecomesOneOfItsPortCalls() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);
    startTrip(vessel);

    vessel.dockAt(port, START.plusHours(3));
    vessel.undock(START.plusHours(4));

    assertThat(vessel.getCurrentTrip().getPortCalls()).containsExactly(
      new PortCall(port, START.plusHours(3), START.plusHours(4))
    );
  }

  @Test
  void endTripEndsTheCurrentTrip() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);
    startTrip(vessel);
    final Trip trip = vessel.getCurrentTrip();
    vessel.dockAt(port, START.plusHours(8));

    vessel.endTrip(START.plusHours(9), List.of());

    assertThat(vessel.getCurrentTrip()).isNull();
    assertThat(trip.getEndDateTime()).isEqualTo(START.plusHours(9));
  }

  @Test
  void startTripStartsTheTripGivenForTheOriginWithItsKind() {
    final Port port = mock(Port.class);
    final Vessel vessel = vesselDockedAt(port);

    vessel.startTrip(origin ->
      new FishingTrip(vessel, origin, new Cell(1, 0), START)
    );

    assertThat(vessel.getCurrentTrip()).isInstanceOf(FishingTrip.class);
    assertThat(vessel.getCurrentTrip().getOrigin()).isSameAs(port);
  }

  @Test
  void aVesselCannotStartTheTripOfAnother() {
    final Vessel vessel = vesselDockedAt(mock(Port.class));
    final Vessel otherVessel = vesselDockedAt(mock(Port.class));

    assertThatThrownBy(() ->
      vessel.startTrip(origin ->
        new Trip(otherVessel, origin, new Cell(1, 0), START)
      )
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("cannot start the trip of");
    assertThat(vessel.getCurrentTrip()).isNull();
    assertThat(vessel.isDocked()).isTrue();
  }

  @Test
  void aRecordKeepsTheAttributesAndHomePortTheVesselHadWhenItWasMade() {
    final Port firstPort = mock(Port.class);
    final Port secondPort = mock(Port.class);
    final PortGrid portGrid = mock(PortGrid.class);
    when(portGrid.getObject("P1")).thenReturn(Optional.of(firstPort));
    when(portGrid.getObject("P2")).thenReturn(Optional.of(secondPort));
    final Vessel vessel = newVessel(
      schedule,
      mock(VesselField.class),
      portGrid,
      behaviour(_ -> behaviour)
    );
    vessel.setStartingState(Optional.of("P1"), Map.of("gear", "PS"));
    vessel.buildComponents();
    final ExtendedFishingAction fishing = new ExtendedFishingAction(
      vessel,
      START,
      ofHours(1),
      new Coordinate(0, 0),
      mock(Gear.class)
    );
    final InstantFishingAction instant = new InstantFishingAction(
      vessel,
      START,
      new Coordinate(0, 0),
      mock(Gear.class)
    );
    final Sale sale = new Sale(START, "sale", null, vessel, List.of(), null);
    final FishingActionEvent event = new FishingActionEvent(fishing, null);

    vessel.queue(new HomePortUpdate(Optional.of("P2")));
    vessel.queue(setting("gear", "OTB"));
    stepFor(ofSeconds(1));

    assertThat(fishing.getAttributes()).containsExactly(entry("gear", "PS"));
    assertThat(sale.getAttributes()).containsExactly(entry("gear", "PS"));
    assertThat(vessel.getAttributes()).containsExactly(entry("gear", "OTB"));
    assertThat(fishing.getHomePort()).isSameAs(firstPort);
    assertThat(instant.getHomePort()).isSameAs(firstPort);
    assertThat(sale.getHomePort()).isSameAs(firstPort);
    assertThat(event.getHomePort()).isSameAs(firstPort);
    assertThat(vessel.getHomePort()).containsSame(secondPort);
  }

  @Test
  void aRecordOfAVesselWithoutAModelledHomePortFailsNamingTheVessel() {
    final Vessel vessel = vesselWithAHomePort(behaviour(_ -> behaviour));
    vessel.setStartingState(Optional.empty(), Map.of());

    assertThatThrownBy(() ->
      new ExtendedFishingAction(
        vessel,
        START,
        ofHours(1),
        new Coordinate(0, 0),
        mock(Gear.class)
      )
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(vessel.getId());
    assertThatThrownBy(() ->
      new InstantFishingAction(
        vessel,
        START,
        new Coordinate(0, 0),
        mock(Gear.class)
      )
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(vessel.getId());
    assertThatThrownBy(() ->
      new Sale(START, "sale", null, vessel, List.of(), null)
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(vessel.getId());
  }

  @Test
  void aVesselHeadsTowardsTheCentreOfACellFromItsPoint() {
    final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
    final PortGrid portGrid = mock(PortGrid.class);
    when(portGrid.getObject(HOME_PORT_CODE)).thenReturn(
      Optional.of(mock(Port.class))
    );
    final Vessel vessel = newVessel(
      schedule,
      vesselField,
      portGrid,
      behaviour(_ -> behaviour)
    );
    vessel.setStartingState(Optional.of(HOME_PORT_CODE), Map.of());
    final Cell destination = new Cell(4, 3);
    when(vesselField.getPoint(vessel)).thenReturn(new Double2D(1, 2));
    when(vesselField.getModelGrid().toPoint(destination)).thenReturn(
      new Double2D(4, 3)
    );

    vessel.setHeadingTowards(destination);

    assertThat(vessel.orientation2D()).isEqualTo(Math.atan2(1, 3));
  }

  @Test
  void aVesselReportsItsPointAsACoordinateAndItsHomePortsCell() {
    final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
    final Port homePort = mock(Port.class);
    final PortGrid portGrid = mock(PortGrid.class);
    when(portGrid.getObject("P1")).thenReturn(Optional.of(homePort));
    when(portGrid.getCell(homePort)).thenReturn(PORT_CELL);
    final Vessel vessel = newVessel(
      schedule,
      vesselField,
      portGrid,
      behaviour(_ -> behaviour)
    );
    vessel.setStartingState(Optional.of("P1"), Map.of());
    final Double2D point = new Double2D(1, 2);
    final Coordinate coordinate = new Coordinate(3.5, 40.5);
    when(vesselField.getPoint(vessel)).thenReturn(point);
    when(vesselField.getModelGrid().toCoordinate(point)).thenReturn(coordinate);

    assertThat(vessel.getCoordinate()).isEqualTo(coordinate);
    assertThat(vessel.getHomePortCell()).contains(PORT_CELL);
  }

  /** Starts a plain trip for {@code vessel}, at {@link #START}. */
  private static void startTrip(final Vessel vessel) {
    vessel.startTrip(origin -> new Trip(vessel, origin, new Cell(1, 0), START));
  }
}
