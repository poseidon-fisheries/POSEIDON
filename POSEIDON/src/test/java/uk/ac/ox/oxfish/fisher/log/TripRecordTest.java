/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.ports.Port;

import static org.mockito.Mockito.mock;


public class TripRecordTest {

    @Test
    public void records() {
        TripRecord record = new TripRecord(1, 0d, 0);
        record.recordCosts(100);
        record.recordEarnings(0, 1, 200);
        record.completeTrip(10, mock(Port.class));
        Assertions.assertEquals(record.getProfitPerHour(false), 10, .001d);
    }


    @Test
    public void opportunityCosts() {


        TripRecord record = new TripRecord(1, 123d, 0);
        record.recordCosts(100);
        record.recordOpportunityCosts(50);
        record.recordEarnings(0, 1, 200);
        record.completeTrip(10, mock(Port.class));
        Assertions.assertEquals(record.getProfitPerHour(false), 10, .001d);
        Assertions.assertEquals(record.getProfitPerHour(true), 5, .001d);
    }

    @Test
    public void profitsAreCorrect() {
        TripRecord record = new TripRecord(2, 123d, 0);
        record.recordCosts(100);
        record.recordEarnings(0, 1, 200);
        record.recordEarnings(1, 1, 100);
        record.completeTrip(10, mock(Port.class));
        Assertions.assertEquals(record.getTotalTripProfit(), 200, .001);

    }
}
