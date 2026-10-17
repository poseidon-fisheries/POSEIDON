/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

/**
 * A value that is the simulation's current date-time, whatever the context,
 * read live from the {@link TemporalSchedule} on every call; it throws once the
 * schedule has run out of events ({@link HasDateTime} says whether it has).
 * Built via {@link Factories#currentDateTime()} in this package.
 */
@RequiredArgsConstructor
public class CurrentDateTime implements Function<Object, LocalDateTime> {

  private final TemporalSchedule temporalSchedule;

  @Override
  public LocalDateTime apply(final Object context) {
    return temporalSchedule.getDateTime();
  }
}
