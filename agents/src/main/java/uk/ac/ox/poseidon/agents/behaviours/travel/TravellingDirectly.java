/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.behaviours.travel;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class TravellingDirectly extends LeafTask<Vessel> {

    private final DistanceCalculator distanceCalculator;

    @Override
    public Task.Status execute() {
        final Vessel vessel = getObject();
        if (getStatus() == RUNNING) {
            vessel.setCurrentCell(vessel.getDestination().getCell());
            return SUCCEEDED;
        } else {
            vessel.setTaskDuration(distanceCalculator.travelDuration(
                vessel.getCell(),
                vessel.getDestination().getCell(),
                vessel.getEngine().getCruisingSpeed()
            ));
            return RUNNING;
        }
    }

    @Override
    protected Task<Vessel> copyTo(final Task<Vessel> task) {
        throw new UnsupportedOperationException();
    }
}
