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

package uk.ac.ox.poseidon.core.providers.temporal;

import uk.ac.ox.poseidon.core.providers.Provider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Provider} of the next occurrence of one of the given times of day strictly after a
 * reference date-time, read anew on every call: the same day if one of the times is still to come,
 * else the earliest of them on the following day. The reference is usually the current date-time
 * ({@link Factories#nextOccurrenceOf}), but can be any other, such as when a vessel departed
 * ({@link Factories#nextOccurrenceAfter}); the occurrence can then already be in the past.
 */
public class NextOccurrenceOfTimeOfDay implements Provider<LocalDateTime> {

    private final Supplier<? extends LocalDateTime> referenceDateTime;
    private final LocalTime[] times;

    /**
     * @param referenceDateTime supplies the date-time the occurrence must be strictly after
     * @param times             the times of day, at least one
     */
    public NextOccurrenceOfTimeOfDay(
        final Supplier<? extends LocalDateTime> referenceDateTime,
        final LocalTime[] times
    ) {
        checkArgument(times.length > 0, "At least one time of day is required.");
        this.referenceDateTime = checkNotNull(referenceDateTime);
        this.times = times.clone();
        Arrays.sort(this.times);
    }

    @Override
    public LocalDateTime get() {
        final LocalDateTime reference = referenceDateTime.get();
        final LocalDate day = reference.toLocalDate();
        final LocalTime timeOfDay = reference.toLocalTime();
        for (final LocalTime time : times) {
            if (time.isAfter(timeOfDay)) {
                return day.atTime(time);
            }
        }
        return day.plusDays(1).atTime(times[0]);
    }
}
