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

import static uk.ac.ox.poseidon.core.utils.Utils.multiStringKey;

import java.util.List;
import java.util.function.BiFunction;

/**
 * A mapping that applies several other mappings to the same thing, under the
 * same context, and joins their results into a single composite string key (via
 * {@code Utils.multiStringKey}). Built via
 * {@link Factories Factories.multiStringKeyFromFunctions(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 */
public class MultiStringKeyFromFunctions<
  C,
  T
> implements BiFunction<C, T, String> {

  private final BiFunction<? super C, ? super T, ?>[] functions;

  /** @param functions the mappings to combine */
  @SuppressWarnings("unchecked")
  public MultiStringKeyFromFunctions(
    final List<? extends BiFunction<? super C, ? super T, ?>> functions
  ) {
    this.functions = functions.toArray(new BiFunction[0]);
  }

  @Override
  public String apply(final C context, final T t) {
    final Object[] keys = new Object[functions.length];
    for (int i = 0; i < functions.length; i++) {
      keys[i] = functions[i].apply(context, t);
    }
    return multiStringKey(keys);
  }
}
