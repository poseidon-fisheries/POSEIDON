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

package uk.ac.ox.poseidon.core.time.windows;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A set of date-times when something is open, such as fishing hours or a
 * market. It answers two questions, each under a context, such as the vessel
 * asking, that it uses to read any live values it depends on. A window that
 * needs no context is asked with {@link Void}.
 *
 * <p>
 * The two answers must agree: when the window is open at a date-time,
 * {@link #earliestOpenDateTime} gives that date-time; when it is closed, it
 * gives a date-time strictly after it, or nothing if the window never opens
 * again. Otherwise a vessel that waits until the window opens and then checks
 * it again could wait zero time for ever, and simulated time would stop.
 * Implementations are not checked at run time.
 *
 * <p>
 * Model date-times have whole-second precision: an implementation rejects a
 * date-time with a nonzero nanosecond part.
 *
 * @param <C> the type of the context
 */
public interface TimeWindow<C> {
  /**
   * @param context  the context, used to read live values
   * @param dateTime the date-time asked about, with no fractional second
   * @return whether the window is open at the date-time
   */
  boolean isOpenAt(C context, LocalDateTime dateTime);

  /**
   * @param context  the context, used to read live values
   * @param dateTime the date-time to start from, with no fractional second
   * @return the earliest date-time at or after the given one when the window is
   *         open: the date-time itself if the window is open then, or empty if
   *         the window never opens again
   */
  Optional<LocalDateTime> earliestOpenDateTime(
    C context,
    LocalDateTime dateTime
  );
}
