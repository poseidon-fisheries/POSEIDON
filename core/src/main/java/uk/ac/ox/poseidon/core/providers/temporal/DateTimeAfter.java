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
import java.time.temporal.TemporalAmount;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A mapping of a reference date-time plus an amount of time, both read anew on
 * every call and both possibly depending on the thing mapped, such as a cell.
 * Built via
 * {@link Factories#dateTimeAfter(uk.ac.ox.poseidon.core.MappingFactory, uk.ac.ox.poseidon.core.MappingFactory)}
 * in this package.
 */
@RequiredArgsConstructor
public class DateTimeAfter<C, T> implements BiFunction<C, T, LocalDateTime> {

  private final @NonNull BiFunction<
    ? super C,
    ? super T,
    ? extends LocalDateTime
  > referenceDateTime;
  private final @NonNull BiFunction<
    ? super C,
    ? super T,
    ? extends TemporalAmount
  > amountToAdd;

  @Override
  public LocalDateTime apply(final C context, final T thing) {
    return referenceDateTime
      .apply(context, thing)
      .plus(amountToAdd.apply(context, thing));
  }
}
