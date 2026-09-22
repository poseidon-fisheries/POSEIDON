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

package uk.ac.ox.poseidon.agents.trips;

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripTest {

    private static Vessel newVessel(
        final Int2D cell,
        final ModelGrid modelGrid
    ) {
        final Vessel vessel = mock(Vessel.class);
        final VesselField vesselField = mock(VesselField.class);
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(vessel.getVesselField()).thenReturn(vesselField);
        when(vesselField.getModelGrid()).thenReturn(modelGrid);
        when(vessel.getCell()).thenReturn(cell);
        when(vessel.getSchedule()).thenReturn(schedule);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.now());
        when(vessel.getEventManager()).thenReturn(mock(EventManager.class));
        return vessel;
    }

    @Test
    void constructorRejectsDestinationOutsideGrid() {
        final ModelGrid modelGrid = mock(ModelGrid.class);
        doThrow(new IllegalArgumentException("outside grid"))
            .when(modelGrid).checkIsInGrid(any(Int2D.class));
        final Vessel vessel = newVessel(new Int2D(0, 0), modelGrid);

        assertThatThrownBy(() -> new Trip(vessel, new Int2D(5, 5)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setDestinationRejectsCellOutsideGrid() {
        // ModelGrid mock's checkIsInGrid no-ops by default, so construction succeeds.
        final ModelGrid modelGrid = mock(ModelGrid.class);
        final Vessel vessel = newVessel(new Int2D(0, 0), modelGrid);
        final Trip trip = new Trip(vessel, new Int2D(1, 1));

        doThrow(new IllegalArgumentException("outside grid"))
            .when(modelGrid).checkIsInGrid(any(Int2D.class));

        assertThatThrownBy(() -> trip.setDestination(new Int2D(9, 9)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setDestinationToTripOriginResetsToOrigin() {
        final ModelGrid modelGrid = mock(ModelGrid.class);
        final Int2D origin = new Int2D(2, 2);
        final Vessel vessel = newVessel(origin, modelGrid);
        final Trip trip = new Trip(vessel, new Int2D(3, 3));

        trip.setDestinationToTripOrigin();

        assertThat(trip.getDestination()).isEqualTo(origin);
    }
}
