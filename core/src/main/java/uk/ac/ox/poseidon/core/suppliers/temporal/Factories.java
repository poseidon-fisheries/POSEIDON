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

package uk.ac.ox.poseidon.core.suppliers.temporal;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Supplier;

public class Factories {

    private Factories() {}

    public static CurrentDateSupplierFactory currentDate() {
        return new CurrentDateSupplierFactory();
    }

    public static CurrentDateTimeSupplierFactory currentDateTime() {
        return new CurrentDateTimeSupplierFactory();
    }

    public static CurrentDayOfWeekSupplierFactory currentDayOfWeek() {
        return new CurrentDayOfWeekSupplierFactory();
    }

    public static CurrentTimeSupplierFactory currentTime() {
        return new CurrentTimeSupplierFactory();
    }

    public static NextDayAtTimeSupplierFactory nextDayAtTime(
        final Factory<? super SimulationScope, ? extends LocalTime> time
    ) {
        return new NextDayAtTimeSupplierFactory(time);
    }

    public static DurationUntilSupplierFactory durationUntil(
        final Factory<? super SimulationScope, ? extends Supplier<LocalDateTime>> referenceDateTime
    ) {
        return new DurationUntilSupplierFactory(referenceDateTime);
    }
}
