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

package uk.ac.ox.poseidon.agents.vessels;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.agents.vessels.SpecificationFixtures.HOME_PORT;
import static uk.ac.ox.poseidon.agents.vessels.SpecificationFixtures.specificationOf;

import com.badlogic.gdx.ai.btree.Task;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.market.Sale;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.VesselTask;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class VesselTest {

  private static final LocalDateTime START = LocalDateTime.of(2013, 1, 1, 0, 0);

  private final TemporalSchedule schedule = new TemporalSchedule(START);
  private final SimState simState = new SimState(0, schedule) {};
  private final RecordingTask behaviour = new RecordingTask();
  private final Vessel vessel = vesselWithItsBehaviour(behaviour);

  /**
   * A behaviour that records when it is run. With a {@link #taskDuration},
   * each run alternates between starting a task of that duration and
   * finishing it; without one, it is never running.
   */
  private class RecordingTask extends VesselTask {

    private final List<LocalDateTime> stepDateTimes = new ArrayList<>();
    private final List<Vessel> vesselsRead = new ArrayList<>();
    private Duration taskDuration;

    @Override
    public Status execute() {
      stepDateTimes.add(schedule.getDateTime());
      vesselsRead.add(getVessel());
      return taskDuration != null && getStatus() != Status.RUNNING
        ? runFor(taskDuration)
        : Status.SUCCEEDED;
    }
  }

  /**
   * @return a vessel on {@link #schedule} with a home port and {@code
   *         behaviour} built, not yet registered as active
   */
  private Vessel vesselWithItsBehaviour(final Task<Vessel> behaviour) {
    final VesselComponent<Task<Vessel>> declaration = behaviour(
      scope -> behaviour
    );
    final Vessel vessel = vesselWithAHomePort(declaration);
    vessel.buildComponent(
      declaration,
      new VesselScope(new SimulationScope(mock(Simulation.class)), vessel)
    );
    return vessel;
  }

  /**
   * @return a vessel on {@link #schedule} with a home port, which steps the
   *         behaviour {@code declaration} declares, with no component built
   */
  private Vessel vesselWithAHomePort(
    final VesselComponent<? extends Task<Vessel>> declaration
  ) {
    final PortGrid portGrid = mock(PortGrid.class);
    when(portGrid.getObject("P1")).thenReturn(Optional.of(mock(Port.class)));
    final Vessel vessel = new Vessel(
      schedule,
      mock(EventManager.class),
      "vessel",
      mock(VesselField.class),
      portGrid,
      mock(MarketGrid.class),
      specificationOf(declaration)
    );
    vessel.setAttributes(Map.of(HOME_PORT, "P1"));
    return vessel;
  }

  /**
   * Keeps the schedule from running out of events, which would stop it.
   */
  @BeforeEach
  void setUp() {
    schedule.scheduleRepeating(_ -> {}, 0, 1.0);
  }

  private void activate() {
    vessel.setRegisteredAsActive(true);
  }

  private void deactivate() {
    vessel.setRegisteredAsActive(false);
  }

  private void stepFor(final Duration duration) {
    schedule.stepFor(simState, duration);
  }

  @Test
  void aNewVesselIsInactive() {
    assertThat(
      vesselWithItsBehaviour(new RecordingTask()).isActive()
    ).isFalse();
  }

  @Test
  void aVesselWhoseBehaviourIsNotBuiltIsInactive() {
    final Vessel vessel = vesselWithAHomePort(behaviour(scope -> behaviour));
    vessel.setRegisteredAsActive(true);
    assertThat(vessel.isActive()).isFalse();
  }

  @Test
  void aVesselThatIsNeverActivatedIsNeverStepped() {
    stepFor(ofSeconds(3));
    assertThat(behaviour.stepDateTimes).isEmpty();
  }

  @Test
  void theBehaviourReachesItsVesselOnlyDuringAStep() {
    activate();
    stepFor(ofSeconds(2));
    assertThat(behaviour.vesselsRead).containsExactly(vessel);
    assertThatThrownBy(behaviour::getVessel)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("read its vessel outside a step");
  }

  @Test
  void activatingAVesselStepsItEveryTickWhileItsBehaviourIsNotRunning() {
    activate();
    stepFor(ofSeconds(4));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(1),
      START.plusSeconds(2),
      START.plusSeconds(3)
    );
  }

  @Test
  void mutatingAnInactiveVesselThatStaysInactiveSchedulesNothing() {
    vessel.mutate(() -> {});
    stepFor(ofSeconds(3));
    assertThat(behaviour.stepDateTimes).isEmpty();
  }

  @Test
  void mutatingAnActiveVesselKeepsASingleStepChain() {
    activate();
    stepFor(ofSeconds(2));
    vessel.mutate(() -> {});
    activate();
    stepFor(ofSeconds(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(1),
      START.plusSeconds(2),
      START.plusSeconds(3)
    );
  }

  @Test
  void reactivatingWhileAStepIsPendingDoesNotScheduleASecondStep() {
    activate();
    deactivate();
    activate();
    stepFor(ofSeconds(3));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(1),
      START.plusSeconds(2)
    );
  }

  @Test
  void aDeactivatedVesselStopsAfterItsPendingStepAndCanBeReactivated() {
    activate();
    stepFor(ofSeconds(2));
    deactivate();
    stepFor(ofSeconds(3));
    activate();
    stepFor(ofSeconds(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(1),
      START.plusSeconds(6)
    );
  }

  @Test
  void aRunningBehaviourIsSteppedAgainAfterItsTaskDuration() {
    behaviour.taskDuration = ofHours(1);
    activate();
    stepFor(ofHours(2));
    assertThat(behaviour.stepDateTimes).startsWith(
      START.plusSeconds(1),
      START.plusHours(1).plusSeconds(1),
      START.plusHours(1).plusSeconds(2)
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
    vesselWithItsBehaviour(new KeepsRunningTask()).setRegisteredAsActive(true);
    stepFor(ofSeconds(2));
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
    vesselWithItsBehaviour(new SetsTwoDurationsTask()).setRegisteredAsActive(
      true
    );
    assertThatThrownBy(() -> stepFor(ofSeconds(2)))
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
    vesselWithItsBehaviour(
      new FinishesWithADurationTask()
    ).setRegisteredAsActive(true);
    assertThatThrownBy(() -> stepFor(ofSeconds(2)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("not running, but a task set a duration");
  }

  @Test
  void mutationsWhileTheBehaviourIsRunningWaitUntilItStopsRunning() {
    behaviour.taskDuration = ofHours(1);
    activate();
    stepFor(ofSeconds(2));
    deactivate();
    assertThat(vessel.isRegisteredAsActive()).isTrue();
    stepFor(ofHours(2));
    assertThat(vessel.isRegisteredAsActive()).isFalse();
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(1),
      START.plusHours(1).plusSeconds(1)
    );
  }

  @Test
  void endTripKeepsTheFinishedTripAsThePreviousTrip() {
    final TemporalSchedule schedule = mock(TemporalSchedule.class);
    when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 7, 0));
    final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
    final PortGrid portGrid = mock(PortGrid.class);
    final Vessel vessel = new Vessel(
      schedule,
      mock(EventManager.class),
      "vessel",
      vesselField,
      portGrid,
      mock(MarketGrid.class),
      specificationOf(behaviour(scope -> behaviour))
    );
    when(vesselField.getCell(vessel)).thenReturn(new Int2D(0, 0));
    when(portGrid.anyObjectsAt(new Int2D(0, 0))).thenReturn(true);
    vessel.setAccount(new Account());

    assertNull(vessel.getPreviousTrip());
    vessel.startTrip(new Int2D(1, 0));
    final Trip trip = vessel.getCurrentTrip();
    vessel.arriveInPort();
    vessel.endTrip();

    assertNull(vessel.getCurrentTrip());
    assertSame(trip, vessel.getPreviousTrip());
  }

  @Test
  void anActionKeepsTheAttributesTheVesselHadWhenItWasMade() {
    vessel.setAttributes(Map.of("gear", "PS"));
    final ExtendedFishingAction fishing = new ExtendedFishingAction(
      vessel,
      START,
      ofHours(1),
      new Coordinate(0, 0),
      mock(Gear.class)
    );
    final Sale sale = new Sale(START, "sale", null, vessel, List.of(), null);
    vessel.setAttributes(Map.of("gear", "OTB"));
    assertThat(fishing.getAttributes()).containsExactly(entry("gear", "PS"));
    assertThat(sale.getAttributes()).containsExactly(entry("gear", "PS"));
    assertThat(vessel.getAttributes()).containsExactly(entry("gear", "OTB"));
  }
}
