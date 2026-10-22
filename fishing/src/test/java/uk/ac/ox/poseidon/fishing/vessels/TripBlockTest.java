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

import static com.badlogic.gdx.ai.btree.Task.Status.FAILED;
import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static java.time.Duration.ofHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.SpecificationFixtures.newVessel;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.tasks.travel.DockAtDestination;
import uk.ac.ox.poseidon.fishing.tasks.travel.SetDestinationToOrigin;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class TripBlockTest {

  private static final Cell PORT = new Cell(0, 0);
  private static final Cell DESTINATION = new Cell(1, 0);
  private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 7, 0);

  private final Port port = mock(Port.class);
  private Vessel vessel;

  /**
   * A task that gives the statuses it is told to, in order, and records the
   * vessel's current trip each time it runs.
   */
  private static class ScriptedTask extends VesselTask {

    private final Deque<Status> statuses;
    private final List<Trip> tripsSeen = new ArrayList<>();

    ScriptedTask(final Status... statuses) {
      this.statuses = new ArrayDeque<>(List.of(statuses));
    }

    @Override
    public Status execute() {
      tripsSeen.add(getVessel().getCurrentTrip());
      final Status status = statuses.remove();
      return status == RUNNING ? runFor(ofHours(1)) : status;
    }
  }

  /**
   * A vessel docked at {@link #port}, on no trip, with an account to fold
   * trips into.
   */
  @BeforeEach
  void setUp() {
    final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
    final PortGrid portGrid = mock(PortGrid.class);
    vessel = newVessel(
      mock(TemporalSchedule.class),
      vesselField,
      portGrid,
      behaviour(_ -> new ScriptedTask())
    );
    when(vesselField.getCell(vessel)).thenReturn(PORT);
    when(portGrid.getCell(port)).thenReturn(PORT);
    vessel.dockAt(port, NOW);
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  private TripBlock tripTo(
    final Optional<Cell> initialDestination,
    final Task<Vessel>... children
  ) {
    return new TripBlock(
      _ -> initialDestination,
      _ -> NOW,
      List.of(),
      List.of(children)
    );
  }

  private BehaviorTree<Vessel> treeOver(final TripBlock tripBlock) {
    return new BehaviorTree<>(tripBlock, vessel);
  }

  @Test
  void opensATripForItsTasksAndClosesItWhenTheyAllSucceed() {
    final ScriptedTask fishing = new ScriptedTask(RUNNING, SUCCEEDED);
    final TripBlock tripBlock = tripTo(
      Optional.of(DESTINATION),
      fishing,
      new SetDestinationToOrigin(),
      new DockAtDestination(_ -> NOW)
    );
    final BehaviorTree<Vessel> tree = treeOver(tripBlock);

    tree.step();
    final Trip trip = vessel.getCurrentTrip();
    assertThat(trip).isNotNull();
    assertThat(trip.getDestination()).isEqualTo(DESTINATION);
    assertThat(trip.getOrigin()).isEqualTo(port);
    assertThat(vessel.isDocked()).isFalse();

    tree.step();
    assertThat(tripBlock.getStatus()).isEqualTo(SUCCEEDED);
    assertThat(fishing.tripsSeen).containsExactly(trip, trip);
    assertThat(vessel.getCurrentTrip()).isNull();
    assertThat(vessel.getPreviousTrip()).isSameAs(trip);
    assertThat(vessel.getPortDockedAt()).isEqualTo(port);
    assertThat(trip.getStartDateTime()).isEqualTo(NOW);
    assertThat(trip.getEndDateTime()).isEqualTo(NOW);
  }

  @Test
  void chargesItsTripCostsInOrderAsItClosesTheTrip() {
    final TripBlock tripBlock = new TripBlock(
      _ -> Optional.of(DESTINATION),
      _ -> NOW,
      List.of(
        _ -> Money.of(CurrencyUnit.EUR, 5),
        _ -> Money.of(CurrencyUnit.EUR, 2)
      ),
      List.of(new SetDestinationToOrigin(), new DockAtDestination(_ -> NOW))
    );

    treeOver(tripBlock).step();

    assertThat(
      vessel.getPreviousTrip().getAccount().getBalances()
    ).containsExactly(entry(CurrencyUnit.EUR, Money.of(CurrencyUnit.EUR, -7)));
  }

  @Test
  void failsWithoutOpeningATripWhenThereIsNoDestination() {
    final ScriptedTask fishing = new ScriptedTask(SUCCEEDED);
    final TripBlock tripBlock = tripTo(Optional.empty(), fishing);

    treeOver(tripBlock).step();

    assertThat(tripBlock.getStatus()).isEqualTo(FAILED);
    assertThat(fishing.tripsSeen).isEmpty();
    assertThat(vessel.getCurrentTrip()).isNull();
    assertThat(vessel.getPreviousTrip()).isNull();
  }

  @Test
  void aTaskFailingWithTheTripOpenThrows() {
    final TripBlock tripBlock = tripTo(
      Optional.of(DESTINATION),
      new ScriptedTask(FAILED)
    );

    assertThatThrownBy(treeOver(tripBlock)::step)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ended as FAILED with its trip open");
  }

  @Test
  void cancellingARunningTripBlockThrows() {
    final TripBlock tripBlock = tripTo(
      Optional.of(DESTINATION),
      new ScriptedTask(RUNNING)
    );
    final BehaviorTree<Vessel> tree = treeOver(tripBlock);
    tree.step();
    tree.setObject(null);

    assertThatThrownBy(tree::resetTask)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ended as CANCELLED with its trip open");
  }

  @Test
  void aTripBlockInsideAnotherThrows() {
    final TripBlock tripBlock = tripTo(
      Optional.of(DESTINATION),
      tripTo(Optional.of(DESTINATION), new DockAtDestination(_ -> NOW))
    );

    assertThatThrownBy(treeOver(tripBlock)::step)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("already on a trip");
  }
}
