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

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.tasks.travel.TravelTasks.*;

class TravelDirectlyTest {

    @Test
    void takesTheDistanceOverTheCruisingSpeed() throws Exception {
        final DistanceCalculator distanceCalculator = mock(DistanceCalculator.class);
        when(distanceCalculator.distanceInKm(ORIGIN, DESTINATION)).thenReturn(5.0);
        final Vessel vessel = vesselCruisingAtTenKph();
        final TravelDirectly travelDirectly = new TravelDirectly(distanceCalculator);
        setTaskObject(travelDirectly, vessel);

        travelDirectly.start();
        travelDirectly.execute();

        verify(vessel).setTaskDuration(Duration.ofMinutes(30));
    }
}
