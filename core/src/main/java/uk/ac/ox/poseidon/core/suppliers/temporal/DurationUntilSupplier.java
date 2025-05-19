/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public class DurationUntilSupplier implements Supplier<Duration> {

    private final TemporalSchedule schedule;
    private final Supplier<? extends LocalDateTime> referenceDateTimeSupplier;

    @Override
    public Duration get() {
        final LocalDateTime currentDateTime = schedule.getDateTime();
        final LocalDateTime referenceDateTime = referenceDateTimeSupplier.get();
        checkState(
            referenceDateTime.isAfter(currentDateTime),
            "Reference date-time (%s) must be after current date-time (%s)",
            referenceDateTime,
            currentDateTime
        );
        return Duration.between(currentDateTime, referenceDateTime);
    }
}
