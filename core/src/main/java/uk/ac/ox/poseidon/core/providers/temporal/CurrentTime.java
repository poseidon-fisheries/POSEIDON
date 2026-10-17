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
import java.time.LocalTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A value that is the simulation's current time of day, read from the current
 * date-time on every call. Built via {@link Factories#currentTime()} in this
 * package.
 */
@RequiredArgsConstructor
public class CurrentTime<C> implements Function<C, LocalTime> {

  private final @NonNull Function<
    ? super C,
    ? extends LocalDateTime
  > currentDateTime;

  @Override
  public LocalTime apply(final C context) {
    return currentDateTime.apply(context).toLocalTime();
  }
}
