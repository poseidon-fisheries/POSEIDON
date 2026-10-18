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

package uk.ac.ox.poseidon.agents.tasks.travel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.vessels.DeclaredComponents.giveComponent;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.FactoryComponent;
import uk.ac.ox.poseidon.agents.vessels.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.core.events.EventManager;

/**
 * Shared set-up for the travel task tests: a vessel at {@link #ORIGIN} cruising
 * at 10 km/h, burning 3 litres per kilometre.
 */
final class TravelTasks {

  static final Int2D ORIGIN = new Int2D(0, 0);
  static final Int2D DESTINATION = new Int2D(1, 0);
  /** The date the clock of the tasks made here reads. */
  static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

  /** The declaration of the engine of the vessels made here. */
  static final VesselComponent<Engine> ENGINE = new FactoryComponent<>(scope ->
    mock(Engine.class)
  );

  /** The declaration of the fuel tank of the vessels made here. */
  static final VesselComponent<FuelTank> FUEL_TANK = new FactoryComponent<>(
    scope -> mock(FuelTank.class)
  );

  private TravelTasks() {}

  static Vessel vesselCruisingAtTenKph() {
    final Vessel vessel = mock(Vessel.class);
    final Engine engine = mock(Engine.class);
    final Trip trip = mock(Trip.class);
    when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
    when(engine.getLitresOfFuelConsumedPerKm()).thenReturn(3.0);
    when(trip.getDestination()).thenReturn(DESTINATION);
    when(trip.getEventManager()).thenReturn(mock(EventManager.class));
    giveComponent(vessel, ENGINE, engine);
    giveComponent(vessel, FUEL_TANK, mock(FuelTank.class));
    when(vessel.getCell()).thenReturn(ORIGIN);
    when(vessel.getCurrentTrip()).thenReturn(trip);
    return vessel;
  }

  static void setTaskObject(final Task<?> task, final Object object)
    throws Exception {
    final BehaviorTree<Object> behaviorTree = new BehaviorTree<>();
    behaviorTree.setObject(object);
    final Field treeField = Task.class.getDeclaredField("tree");
    treeField.setAccessible(true);
    treeField.set(task, behaviorTree);
  }
}
