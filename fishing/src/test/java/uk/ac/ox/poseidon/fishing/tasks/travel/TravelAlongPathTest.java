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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.tasks.travel.TravelTasks.*;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task.Status;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.vessels.Trip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

class TravelAlongPathTest {

  @Test
  void takesEachHopsDistanceOverTheCruisingSpeed() throws Exception {
    final DistanceCalculator distanceCalculator = mock(
      DistanceCalculator.class
    );
    when(distanceCalculator.distanceInKm(ORIGIN, DESTINATION)).thenReturn(5.0);
    final Vessel vessel = vesselCruisingAtTenKph();
    final TravelAlongPath travelAlongPath = travelingFromOriginToDestination(
      distanceCalculator
    );
    setTaskObject(travelAlongPath, vessel);

    travelAlongPath.start();
    travelAlongPath.execute();

    verify(vessel).setTaskDuration(Duration.ofMinutes(30));
  }

  @Test
  void takesTheFuelTheEngineBurnsOverEachHopFromTheTank() throws Exception {
    final DistanceCalculator distanceCalculator = mock(
      DistanceCalculator.class
    );
    when(distanceCalculator.distanceInKm(ORIGIN, DESTINATION)).thenReturn(5.0);
    final Vessel vessel = vesselCruisingAtTenKph();
    final TravelAlongPath travelAlongPath = travelingFromOriginToDestination(
      distanceCalculator
    );
    setTaskObject(travelAlongPath, vessel);

    travelAlongPath.start();
    travelAlongPath.execute();
    travelAlongPath.execute();

    verify(FUEL_TANK.of(vessel)).consumeFuel(15.0);
  }

  @Test
  void travelsOutAndBackAsOneTaskInTwoPlacesOfATree() {
    final Vessel vessel = vesselCruisingAtTenKph();
    final AtomicReference<Int2D> cell = new AtomicReference<>(ORIGIN);
    when(vessel.getCell()).thenAnswer(_ -> cell.get());
    doAnswer(invocation -> {
      cell.set(invocation.getArgument(0));
      return null;
    })
      .when(vessel)
      .setCurrentCell(any());
    final AtomicReference<Int2D> destination = new AtomicReference<>(
      DESTINATION
    );
    final Trip trip = vessel.getCurrentTrip();
    when(trip.getDestination()).thenAnswer(_ -> destination.get());
    doAnswer(_ -> {
      destination.set(ORIGIN);
      return null;
    })
      .when(trip)
      .setDestinationToTripOrigin();
    final DistanceCalculator distanceCalculator = mock(
      DistanceCalculator.class
    );
    when(
      distanceCalculator.distanceInKm(any(Int2D.class), any(Int2D.class))
    ).thenReturn(5.0);
    @SuppressWarnings("unchecked")
    final PathFinder<Int2D> pathFinder = mock(PathFinder.class);
    when(pathFinder.getPath(ORIGIN, DESTINATION)).thenReturn(
      Optional.of(ImmutableList.of(ORIGIN, DESTINATION))
    );
    when(pathFinder.getPath(DESTINATION, ORIGIN)).thenReturn(
      Optional.of(ImmutableList.of(DESTINATION, ORIGIN))
    );
    final List<TravelEvent> travelEvents = new ArrayList<>();
    final EventManager eventManager = new SimpleEventManager();
    eventManager.addListener(
      new SimpleListener<>(TravelEvent.class, travelEvents::add)
    );
    final TravelAlongPath travel = new TravelAlongPath(
      ENGINE,
      FUEL_TANK,
      pathFinder,
      distanceCalculator,
      v -> v.getCurrentTrip().getDestination(),
      v -> eventManager,
      v -> NOW
    );
    @SuppressWarnings("unchecked")
    final BehaviorTree<Vessel> tree = new BehaviorTree<>(
      new Sequence<>(travel, new SetDestinationToOrigin(), travel)
    );

    do {
      tree.setObject(vessel);
      tree.step();
      tree.setObject(null);
    } while (tree.getStatus() == Status.RUNNING);

    assertThat(travelEvents)
      .extracting(TravelEvent::getOrigin, TravelEvent::getDestination)
      .containsExactly(tuple(ORIGIN, DESTINATION), tuple(DESTINATION, ORIGIN));
    assertThat(cell.get()).isEqualTo(ORIGIN);
    assertThatThrownBy(travel::getVessel)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("read its vessel outside a step");
  }

  private static TravelAlongPath travelingFromOriginToDestination(
    final DistanceCalculator distanceCalculator
  ) {
    @SuppressWarnings("unchecked")
    final PathFinder<Int2D> pathFinder = mock(PathFinder.class);
    when(pathFinder.getPath(ORIGIN, DESTINATION)).thenReturn(
      Optional.of(ImmutableList.of(ORIGIN, DESTINATION))
    );
    return new TravelAlongPath(
      ENGINE,
      FUEL_TANK,
      pathFinder,
      distanceCalculator,
      v -> DESTINATION,
      v -> mock(EventManager.class),
      v -> NOW
    );
  }
}
