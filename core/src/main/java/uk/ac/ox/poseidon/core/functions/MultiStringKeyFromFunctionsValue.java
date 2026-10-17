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
import java.util.function.Function;

/**
 * A value that reads several other values under the same context and joins them
 * into a single composite string key (via {@code Utils.multiStringKey}). Built
 * via
 * {@link Factories#multiStringKeyFromFunctions(uk.ac.ox.poseidon.core.ValueFactory...)}
 * in this package.
 *
 * @param <C> the type of the context
 */
public class MultiStringKeyFromFunctionsValue<C> implements
  Function<C, String> {

  private final Function<? super C, ?>[] functions;

  /** @param functions the values to combine */
  @SuppressWarnings("unchecked")
  public MultiStringKeyFromFunctionsValue(
    final List<? extends Function<? super C, ?>> functions
  ) {
    this.functions = functions.toArray(new Function[0]);
  }

  @Override
  public String apply(final C context) {
    final Object[] keys = new Object[functions.length];
    for (int i = 0; i < functions.length; i++) {
      keys[i] = functions[i].apply(context);
    }
    return multiStringKey(keys);
  }
}
