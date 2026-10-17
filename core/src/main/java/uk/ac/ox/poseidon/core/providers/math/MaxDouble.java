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
 * A value that is the greater of two double values, both asked under the same
 * context, recomputed on every call, as by {@link Math#max}. Built via
 * {@link Factories#maxDouble(uk.ac.ox.poseidon.core.ValueFactory, uk.ac.ox.poseidon.core.ValueFactory)}
 * in this package.
 *
 * @param <C> the type of the context
 */
@RequiredArgsConstructor
public class MaxDouble<C> implements Function<C, Double> {

  @NonNull
  private final Function<? super C, Double> a;

  @NonNull
  private final Function<? super C, Double> b;

  @Override
  public Double apply(final C context) {
    return Math.max(a.apply(context), b.apply(context));
  }
}
