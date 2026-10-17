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

package uk.ac.ox.poseidon.core.predicates.comparable;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A yes/no mapping that is true iff the thing tested lies between a minimum and
 * a maximum, both read under the context, and each independently inclusive or
 * exclusive. Built via {@link Factories Factories.between(...)} in this
 * package.
 *
 * @param <C> the type of the context the reference is read under
 * @param <T> the {@link Comparable} type of the bounds
 */
@RequiredArgsConstructor
public class Between<C, T> implements BiFunction<C, Comparable<T>, Boolean> {

  private final @NonNull Function<? super C, ? extends T> minimum;
  private final @NonNull Function<? super C, ? extends T> maximum;
  private final boolean includeMinimum;
  private final boolean includeMaximum;

  @Override
  public Boolean apply(final C context, final Comparable<T> mapped) {
    final int comparedToMinimum = mapped.compareTo(minimum.apply(context));
    final int comparedToMaximum = mapped.compareTo(maximum.apply(context));
    return (
      (includeMinimum ? comparedToMinimum >= 0 : comparedToMinimum > 0) &&
      (includeMaximum ? comparedToMaximum <= 0 : comparedToMaximum < 0)
    );
  }
}
