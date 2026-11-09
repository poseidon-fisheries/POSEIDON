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
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A mapping that looks a key up in a fixed, resolved {@link NavigableMap} on
 * every call, like {@link MapLookup}, but a key with no entry of its own gives
 * the entry with the nearest earlier key ({@link NavigableMap#floorEntry}). A
 * key below every entry throws a {@link NoSuchElementException} naming the
 * key. Keyed on the current date, it turns a series of dated values into a
 * value that steps from one entry to the next. Built via
 * {@link Factories Factories.floorMapLookup(...)} in this package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <K> the type of the key
 * @param <V> the type of the value looked up
 */
@RequiredArgsConstructor
public class FloorMapLookup<C, T, K, V> implements BiFunction<C, T, V> {

  private final @NonNull BiFunction<? super C, ? super T, ? extends K> key;
  private final @NonNull NavigableMap<? super K, ? extends V> map;

  @Override
  public V apply(final C context, final T t) {
    final K k = key.apply(context, t);
    final Map.Entry<? super K, ? extends V> entry = map.floorEntry(k);
    if (entry == null) {
      throw new NoSuchElementException(
        String.format("No entry for key %s.", k)
      );
    }
    return entry.getValue();
  }
}
