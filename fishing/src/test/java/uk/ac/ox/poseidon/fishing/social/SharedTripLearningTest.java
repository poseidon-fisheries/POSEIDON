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

package uk.ac.ox.poseidon.fishing.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.choices.Factories.fishingTripEndRecorder;
import static uk.ac.ox.poseidon.fishing.choices.Factories.keepLastReceived;
import static uk.ac.ox.poseidon.fishing.social.Factories.fishingTripEndSharer;
import static uk.ac.ox.poseidon.fishing.social.Factories.sharedFishingTripEndListener;
import static uk.ac.ox.poseidon.fishing.social.Factories.socialNetwork;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselEventListener;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentScopeOf;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.choices.MemoryFactory;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Fleet;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A vessel's own and shared trip ends are recorded by one recorder into one
 * memory of the last received value per cell.
 */
class SharedTripLearningTest {

  private final Vessel a = mock(Vessel.class);
  private final Vessel b = mock(Vessel.class);
  private final Cell cell = mock(Cell.class);
  private final Simulation simulation = simulation();
  private final SimulationScope simulationScope = new SimulationScope(
    simulation
  );
  private final SocialNetworkFactory network = socialNetwork(_ ->
    fleetOf(a, b)
  );
  private final EventSharerFactory<FishingTripEndEvent> tripSharer =
    fishingTripEndSharer(network);
  private final MemoryFactory<Cell, Double> memory = new MemoryFactory<>();
  private final ValueFactory<Scope, Object, Optional<Cell>> destinationCell =
    _ -> _ -> Optional.of(cell);
  private final ValueFactory<Scope, FishingTripEndEvent, Double> hoursAtSea =
    _ -> event -> (double) event.getTrip().getTotalTimeAtSea().toHours();
  private final VesselComponentScope scopeB;

  SharedTripLearningTest() {
    when(a.getProductStore()).thenReturn(new ProductStore());
    when(b.getProductStore()).thenReturn(new ProductStore());
    when(b.getEventManager()).thenReturn(new SimpleEventManager());
    scopeB = componentScopeOf(
      new VesselScope(simulationScope, b),
      new ProductStore()
    );
    final var tripRecorder = fishingTripEndRecorder(
      destinationCell,
      hoursAtSea,
      memory,
      keepLastReceived()
    );
    vesselEventListener(tripRecorder).get(scopeB);
    sharedFishingTripEndListener(tripRecorder, tripSharer).get(scopeB);
    network.get(simulationScope).addTie(a, b);
  }

  private static Simulation simulation() {
    final Simulation simulation = mock(Simulation.class);
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(simulation.getEventManager()).thenReturn(new SimpleEventManager());
    return simulation;
  }

  private static Fleet fleetOf(final Vessel... vessels) {
    final Fleet fleet = mock(Fleet.class);
    when(fleet.getVessels()).thenReturn(ImmutableSet.copyOf(vessels));
    return fleet;
  }

  private static FishingTripEndEvent tripEnd(
    final Vessel vessel,
    final int hoursAtSea,
    final LocalDateTime endDateTime
  ) {
    final FishingTrip trip = mock(FishingTrip.class);
    when(trip.getVessel()).thenReturn(vessel);
    when(trip.getTotalTimeAtSea()).thenReturn(Duration.ofHours(hoursAtSea));
    when(trip.getEndDateTime()).thenReturn(endDateTime);
    return new FishingTripEndEvent(trip);
  }

  private Map<Cell, Double> remembered() {
    final Map<Cell, Double> entries = new HashMap<>();
    memory.get(scopeB).apply(null).forEachEntry(entries::put);
    return entries;
  }

  private void friendReports(final FishingTripEndEvent event) {
    tripSharer.get(simulationScope).receive(event);
  }

  @Test
  void ownAndSharedTripsFeedTheSameMemoryTheLastReceivedStanding() {
    final LocalDateTime noon = LocalDateTime.of(2026, 1, 1, 12, 0);
    b.getEventManager().broadcast(tripEnd(b, 3, noon));
    assertThat(remembered()).containsEntry(cell, 3.0);

    // An older trip received later still replaces the newer one
    friendReports(tripEnd(a, 7, noon.minusHours(5)));
    assertThat(remembered()).containsEntry(cell, 7.0);

    b.getEventManager().broadcast(tripEnd(b, 5, noon.plusHours(1)));
    assertThat(remembered()).containsEntry(cell, 5.0);
  }

  @Test
  void aDroppedTieKeepsWhatWasRememberedAndANewTieGivesNoHistory() {
    final LocalDateTime noon = LocalDateTime.of(2026, 1, 1, 12, 0);
    friendReports(tripEnd(a, 7, noon));

    network.get(simulationScope).removeTie(a, b);
    assertThat(remembered()).containsEntry(cell, 7.0);

    friendReports(tripEnd(a, 9, noon.plusHours(1)));
    network.get(simulationScope).addTie(a, b);
    assertThat(remembered()).containsEntry(cell, 7.0);
  }
}
