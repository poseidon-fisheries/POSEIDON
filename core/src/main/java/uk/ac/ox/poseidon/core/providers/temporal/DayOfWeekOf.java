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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A value that is the day of the week of a date-time, read from it anew on every call. Built
 * via {@link Factories#dayOfWeekOf(uk.ac.ox.poseidon.core.ValueFactory)} in this
 * package.
 */
@RequiredArgsConstructor
public class DayOfWeekOf<C> implements Function<C, DayOfWeek> {

  private final @NonNull Function<? super C, ? extends LocalDateTime> dateTime;

  @Override
  public DayOfWeek apply(final C context) {
    return dateTime.apply(context).getDayOfWeek();
  }
}
