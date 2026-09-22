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

package uk.ac.ox.poseidon.agents.vessels.extractors;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TripCostFromHourlyCostsTest {

    @Test
    void appliesHourlyRateToTripDurationInHours() {
        final CurrencyUnit eur = CurrencyUnit.of("EUR");
        final Money hourlyCost = Money.of(eur, 10.0);

        final Vessel vessel = mock(Vessel.class);
        final Trip trip = mock(Trip.class);
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(vessel.getCurrentTrip()).thenReturn(trip);
        when(vessel.getSchedule()).thenReturn(schedule);
        when(trip.getStartDateTime()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2024, 1, 1, 3, 0));

        final TripCostFromHourlyCosts tripCost =
            new TripCostFromHourlyCosts(v -> hourlyCost);

        assertThat(tripCost.apply(vessel)).isEqualTo(Money.of(eur, 30.0));
    }
}
