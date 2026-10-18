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
import static uk.ac.ox.poseidon.agents.vessels.SpecificationFixtures.newVessel;

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
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.Sale;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.tasks.VesselTask;
import uk.ac.ox.poseidon.agents.vessels.VesselStatement.AttributeChange;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
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
   *         behaviour} built, whose first step is not yet scheduled
   */
  private Vessel vesselWithItsBehaviour(final Task<Vessel> behaviour) {
    final VesselComponent<Task<Vessel>> declaration = behaviour(
      scope -> behaviour
    );
    final Vessel vessel = vesselWithAHomePort(declaration);
    vessel.buildComponents();
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
    final Vessel vessel = newVessel(
      schedule,
      mock(VesselField.class),
      portGrid,
      declaration
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

  private static AttributeChange setting(
    final String attribute,
    final Object value
  ) {
    return new AttributeChange(
      ImmutableMap.of(attribute, value),
      ImmutableSet.of()
    );
  }

  private static AttributeChange removing(final String attribute) {
    return new AttributeChange(ImmutableMap.of(), ImmutableSet.of(attribute));
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
    vessel.setAttributes(Map.of());
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
  void aVesselStepsFirstAtOnceThenEveryTickWhileItsBehaviourIsNotRunning() {
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(3));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START,
      START.plusSeconds(1),
      START.plusSeconds(2)
    );
  }

  @Test
  void aStatementOnAVesselWithNoStepPendingSchedulesOneAtOnce() {
    vessel.setAttributes(Map.of());
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(2));
    vessel.queue(setting(HOME_PORT, "P1"));
    assertThat(vessel.getAttributes()).doesNotContainKey(HOME_PORT);
    stepFor(ofSeconds(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START.plusSeconds(2),
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
      START.plusSeconds(2),
      START.plusSeconds(3)
    );
    assertThat(vessel.getAttributes()).containsEntry("gear", "OTB");
  }

  @Test
  void aVesselThatLosesItsHomePortStopsUntilAStatementGivesItOneBack() {
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(2));
    vessel.queue(removing(HOME_PORT));
    stepFor(ofSeconds(3));
    vessel.queue(setting(HOME_PORT, "P1"));
    stepFor(ofSeconds(2));
    assertThat(behaviour.stepDateTimes).containsExactly(
      START,
      START.plusSeconds(1),
      START.plusSeconds(5),
      START.plusSeconds(6)
    );
  }

  @Test
  void aStatementMadeDuringTheVesselsOwnStepIsAppliedAtItsNextStep() {
    final class StatesItsGearTask extends VesselTask {

      @Override
      public Status execute() {
        getVessel().queue(setting("gear", "PS"));
        return Status.SUCCEEDED;
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
    assertThat(behaviour.stepDateTimes).startsWith(
      START,
      START.plusHours(1),
      START.plusHours(1).plusSeconds(1)
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
  void aStatementWaitsForTheStepAfterTheBehaviourStopsRunning() {
    behaviour.taskDuration = ofHours(1);
    vessel.scheduleFirstStep();
    stepFor(ofSeconds(1));
    vessel.queue(setting("gear", "PS"));
    // Up to the step at one hour, in which the behaviour stops running
    stepFor(ofHours(1));
    assertThat(vessel.getAttributes()).doesNotContainKey("gear");
    stepFor(ofSeconds(1));
    assertThat(vessel.getAttributes()).containsEntry("gear", "PS");
  }

  @Test
  void endTripKeepsTheFinishedTripAsThePreviousTrip() {
    final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
    final PortGrid portGrid = mock(PortGrid.class);
    final Vessel vessel = newVessel(
      mock(TemporalSchedule.class),
      vesselField,
      portGrid,
      behaviour(scope -> behaviour)
    );
    when(vesselField.getCell(vessel)).thenReturn(new Int2D(0, 0));
    when(portGrid.anyObjectsAt(new Int2D(0, 0))).thenReturn(true);

    assertNull(vessel.getPreviousTrip());
    vessel.startTrip(new Int2D(1, 0), START);
    final Trip trip = vessel.getCurrentTrip();
    vessel.arriveInPort(START.plusHours(8));
    vessel.endTrip(START.plusHours(9));

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
