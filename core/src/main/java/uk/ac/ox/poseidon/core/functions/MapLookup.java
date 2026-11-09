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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A mapping that looks a key up in a fixed, resolved {@link Map} on every call:
 * the key is read from the thing mapped, under the context. Unlike
 * {@link MapEntryFactory}, the key is read per call rather than fixed at
 * resolution time. A key with no entry gives the default value if there is one,
 * and otherwise throws a {@link NoSuchElementException} naming the key. Built
 * via {@link Factories Factories.mapLookup(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <K> the type of the key
 * @param <V> the type of the value looked up
 */
@RequiredArgsConstructor
public class MapLookup<C, T, K, V> implements BiFunction<C, T, V> {

  private final @NonNull BiFunction<? super C, ? super T, ? extends K> key;
  private final @NonNull Map<? super K, ? extends V> map;

  /** The value for a key with no entry, or {@code null} to throw instead. */
  private final V defaultValue;

  @Override
  public V apply(final C context, final T t) {
    final K k = key.apply(context, t);
    final V value = map.get(k);
    final boolean missing = value == null && !map.containsKey(k);
    if (missing && defaultValue == null) {
      throw new NoSuchElementException(
        String.format("No entry for key %s.", k)
      );
    }
    return missing ? defaultValue : value;
  }
}
