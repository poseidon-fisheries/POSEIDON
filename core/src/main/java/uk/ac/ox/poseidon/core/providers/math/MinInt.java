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

package uk.ac.ox.poseidon.core.providers.math;

import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A value that is the smaller of two integer values, both asked under the same
 * context, recomputed on every call, as by {@link Math#min}. Built via
 * {@link Factories#minInt(uk.ac.ox.poseidon.core.ValueFactory, uk.ac.ox.poseidon.core.ValueFactory)}
 * in this package.
 *
 * @param <C> the type of the context
 */
@RequiredArgsConstructor
public class MinInt<C> implements Function<C, Integer> {

  @NonNull
  private final Function<? super C, Integer> a;

  @NonNull
  private final Function<? super C, Integer> b;

  @Override
  public Integer apply(final C context) {
    return Math.min(a.apply(context), b.apply(context));
  }
}
