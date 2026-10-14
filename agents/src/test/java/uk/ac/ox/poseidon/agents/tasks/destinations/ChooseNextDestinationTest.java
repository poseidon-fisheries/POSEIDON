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

package uk.ac.ox.poseidon.agents.tasks.destinations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task.Status;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

class ChooseNextDestinationTest {

  private static final Int2D NEXT = new Int2D(3, 4);

  private final Vessel vessel = mock(Vessel.class);
  private final Trip trip = mock(Trip.class);

  /** Runs the task once as the root of a behaviour tree over the vessel. */
  private Status run(final ChooseNextDestination chooseNextDestination) {
    final BehaviorTree<Vessel> tree = new BehaviorTree<>(
      chooseNextDestination,
      vessel
    );
    tree.step();
    return chooseNextDestination.getStatus();
  }

  @Test
  void redirectsTheCurrentTripToTheChosenDestinationAndSucceeds() {
    when(vessel.getCurrentTrip()).thenReturn(trip);

    assertThat(
      run(new ChooseNextDestination(() -> Optional.of(NEXT)))
    ).isEqualTo(Status.SUCCEEDED);
    verify(trip).setDestination(NEXT);
  }

  @Test
  void failsAndLeavesTheTripAloneWhenNoDestinationIsChosen() {
    when(vessel.getCurrentTrip()).thenReturn(trip);

    assertThat(run(new ChooseNextDestination(Optional::empty))).isEqualTo(
      Status.FAILED
    );
    verify(trip, never()).setDestination(any());
  }

  @Test
  void throwsWhenTheVesselIsNotOnATrip() {
    when(vessel.getCurrentTrip()).thenReturn(null);

    assertThatThrownBy(() ->
      run(new ChooseNextDestination(() -> Optional.of(NEXT)))
    ).isInstanceOf(IllegalStateException.class);
  }
}
