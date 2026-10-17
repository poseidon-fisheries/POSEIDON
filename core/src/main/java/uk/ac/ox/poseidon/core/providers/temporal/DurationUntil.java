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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A value of the time left from the current date-time until a reference
 * date-time, both read anew on every call: negative once the reference has
 * passed, so a deadline that has gone by leaves no time. Built via
 * {@link Factories#durationUntil(uk.ac.ox.poseidon.core.ValueFactory)} in this
 * package.
 */
@RequiredArgsConstructor
public class DurationUntil<C> implements Function<C, Duration> {

  private final @NonNull Function<
    ? super C,
    ? extends LocalDateTime
  > currentDateTime;
  private final @NonNull Function<
    ? super C,
    ? extends LocalDateTime
  > referenceDateTime;

  @Override
  public Duration apply(final C context) {
    return Duration.between(
      currentDateTime.apply(context),
      referenceDateTime.apply(context)
    );
  }
}
