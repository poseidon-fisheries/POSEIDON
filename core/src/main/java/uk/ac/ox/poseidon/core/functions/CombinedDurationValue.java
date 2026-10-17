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
import java.util.function.Function;

/**
 * A value that sums the {@link Duration}s of several other values read under
 * the same context. Built via
 * {@link Factories#combinedDuration(uk.ac.ox.poseidon.core.ValueFactory...)} in
 * this package.
 *
 * @param <C> the type of the context
 */
public class CombinedDurationValue<C> implements Function<C, Duration> {

  private final Function<? super C, ? extends Duration>[] durations;

  /** @param durations the values to combine */
  @SuppressWarnings("unchecked")
  public CombinedDurationValue(
    final List<? extends Function<? super C, ? extends Duration>> durations
  ) {
    this.durations = durations.toArray(new Function[0]);
  }

  @Override
  public Duration apply(final C context) {
    Duration total = Duration.ZERO;
    for (int i = 0; i < durations.length; i++) {
      total = total.plus(durations[i].apply(context));
    }
    return total;
  }
}
