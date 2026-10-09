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

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Shared set-up for the travel task tests: a vessel at {@link #ORIGIN} cruising at 10 km/h. */
final class TravelTasks {

    static final Int2D ORIGIN = new Int2D(0, 0);
    static final Int2D DESTINATION = new Int2D(1, 0);

    private TravelTasks() {
    }

    static Vessel vesselCruisingAtTenKph() {
        final Vessel vessel = mock(Vessel.class);
        final Engine engine = mock(Engine.class);
        final Trip trip = mock(Trip.class);
        when(engine.getCruisingSpeedInKph()).thenReturn(10.0);
        when(trip.getDestination()).thenReturn(DESTINATION);
        when(vessel.getEngine()).thenReturn(engine);
        when(vessel.getCell()).thenReturn(ORIGIN);
        when(vessel.getCurrentTrip()).thenReturn(trip);
        when(vessel.getSchedule()).thenReturn(mock(TemporalSchedule.class));
        return vessel;
    }

    static void setTaskObject(final Task<?> task, final Object object) throws Exception {
        final BehaviorTree<Object> behaviorTree = new BehaviorTree<>();
        behaviorTree.setObject(object);
        final Field treeField = Task.class.getDeclaredField("tree");
        treeField.setAccessible(true);
        treeField.set(task, behaviorTree);
    }
}
