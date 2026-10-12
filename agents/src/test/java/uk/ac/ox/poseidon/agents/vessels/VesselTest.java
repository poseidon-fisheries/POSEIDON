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

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VesselTest {

    @Test
    void endTripKeepsTheFinishedTripAsThePreviousTrip() {
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 7, 0));
        final VesselField vesselField = mock(VesselField.class, RETURNS_DEEP_STUBS);
        final Vessel vessel = new Vessel(
            schedule,
            mock(EventManager.class),
            "vessel",
            vesselField,
            mock(PortGrid.class),
            mock(MarketGrid.class)
        );
        when(vesselField.getCell(vessel)).thenReturn(new Int2D(0, 0));
        vessel.setAccount(new Account());

        assertNull(vessel.getPreviousTrip());
        vessel.startTrip(new Int2D(1, 0));
        final Trip trip = vessel.getCurrentTrip();
        vessel.endTrip();

        assertNull(vessel.getCurrentTrip());
        assertSame(trip, vessel.getPreviousTrip());
    }
}
