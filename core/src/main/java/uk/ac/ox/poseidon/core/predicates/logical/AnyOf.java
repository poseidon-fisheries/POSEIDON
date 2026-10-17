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

package uk.ac.ox.poseidon.core.predicates.logical;

import java.util.Collection;
import java.util.function.BiFunction;
import lombok.NonNull;

/**
 * A yes/no mapping that is true iff at least one of its component mappings is
 * true of the thing tested, under the same context (false for zero predicates).
 * Built via {@link Factories#anyOf(uk.ac.ox.poseidon.core.MappingFactory...)}
 * in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing tested
 */
public class AnyOf<C, T> implements BiFunction<C, T, Boolean> {

  // Using an array for performance reasons
  @NonNull
  private final BiFunction<? super C, ? super T, Boolean>[] predicates;

  /** @param predicates the component mappings */
  @SuppressWarnings("unchecked")
  public AnyOf(
    final Collection<
      ? extends BiFunction<? super C, ? super T, Boolean>
    > predicates
  ) {
    this.predicates = predicates.toArray(BiFunction[]::new);
  }

  @Override
  public Boolean apply(final C context, final T t) {
    for (final BiFunction<
      ? super C,
      ? super T,
      Boolean
    > predicate : predicates) {
      if (predicate.apply(context, t)) {
        return true;
      }
    }
    return false;
  }
}
