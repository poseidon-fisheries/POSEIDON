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

import static com.google.common.base.Preconditions.checkArgument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A {@link TimeWindow} that comes back every day between a start and an end
 * time of day. It is open from the start, inclusive, to the end, exclusive. If
 * the start is after the end, the window crosses midnight, so it is open from
 * the start until midnight and from midnight until the end. If both are equal,
 * it is open all day: a window that is never open could not be waited for, and
 * a context with no hours has no window. It always opens again within a day.
 *
 * <p>
 * Both times are live values, read on every query with the context given, even
 * when asked about a later date-time. For a future date-time, this is an
 * approximation: if the times change between now and then, the answer uses the
 * times as they are now. Both are validated on every query, since a live value
 * can become invalid after the window is built: a time with a fractional second
 * is rejected, as is a date-time with one.
 *
 * @param <C> the type of the context
 */
@RequiredArgsConstructor
public class DailyTimeWindow<C> implements TimeWindow<C> {

  private final @NonNull Function<? super C, ? extends LocalTime> startTime;
  private final @NonNull Function<? super C, ? extends LocalTime> endTime;

  @Override
  public boolean isOpenAt(final C context, final LocalDateTime dateTime) {
    return isOpen(startTime.apply(context), endTime.apply(context), dateTime);
  }

  private static boolean isOpen(
    final LocalTime start,
    final LocalTime end,
    final LocalDateTime dateTime
  ) {
    checkArgument(
      dateTime.getNano() == 0,
      "The date-time %s has a fractional second.",
      dateTime
    );
    checkArgument(
      start.getNano() == 0,
      "The start time %s has a fractional second.",
      start
    );
    checkArgument(
      end.getNano() == 0,
      "The end time %s has a fractional second.",
      end
    );
    final LocalTime timeOfDay = dateTime.toLocalTime();
    final boolean open;
    if (start.equals(end)) {
      open = true;
    } else if (start.isBefore(end)) {
      open = !timeOfDay.isBefore(start) && timeOfDay.isBefore(end);
    } else {
      open = !timeOfDay.isBefore(start) || timeOfDay.isBefore(end);
    }
    return open;
  }

  @Override
  public Optional<LocalDateTime> earliestOpenDateTime(
    final C context,
    final LocalDateTime dateTime
  ) {
    final LocalTime start = startTime.apply(context);
    final LocalDateTime earliest;
    if (isOpen(start, endTime.apply(context), dateTime)) {
      earliest = dateTime;
    } else {
      final LocalDate day = dateTime.toLocalDate();
      earliest = dateTime.toLocalTime().isBefore(start)
        ? day.atTime(start)
        : day.plusDays(1).atTime(start);
    }
    return Optional.of(earliest);
  }
}
