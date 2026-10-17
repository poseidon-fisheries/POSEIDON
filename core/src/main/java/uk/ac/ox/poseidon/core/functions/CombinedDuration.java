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

package uk.ac.ox.poseidon.core.functions;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A mapping that sums the {@link Duration}s produced by several other mappings
 * of the same thing, under the same context. Built via
 * {@link Factories Factories.combinedDuration(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 */
public class CombinedDuration<C, T> implements BiFunction<C, T, Duration> {

  private final BiFunction<
    ? super C,
    ? super T,
    ? extends Duration
  >[] durations;

  /** @param durations the mappings to combine */
  @SuppressWarnings("unchecked")
  public CombinedDuration(
    final List<
      ? extends BiFunction<? super C, ? super T, ? extends Duration>
    > durations
  ) {
    this.durations = durations.toArray(new BiFunction[0]);
  }

  @Override
  public Duration apply(final C context, final T t) {
    Duration total = Duration.ZERO;
    for (int i = 0; i < durations.length; i++) {
      total = total.plus(durations[i].apply(context, t));
    }
    return total;
  }
}
