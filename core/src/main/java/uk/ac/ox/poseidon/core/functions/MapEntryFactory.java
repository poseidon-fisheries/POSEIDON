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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A {@link RelativeScopeFactory} that resolves a fixed key against a resolved
 * {@link Map} and returns the corresponding value, once, at resolution time —
 * unlike {@link MapLookup}/{@link MapLookupFactory}, which resolve the map
 * once but look up the key on every call. There's no separate
 * plain component class here: the produced value {@code V} is returned as-is,
 * with no wrapper type to carry documentation, so this factory carries the
 * behavior doc directly. A key with no entry throws a
 * {@link NoSuchElementException} naming the key when the factory is resolved;
 * there is no default. Built via {@link Factories#mapEntry(Factory, Object)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class MapEntryFactory<
  S extends Scope,
  K,
  V
> extends RelativeScopeFactory<S, V> {

  private final Factory<? super S, ? extends Map<? super K, ? extends V>> map;
  private final K key;

  @Override
  protected V newInstance(final S scope) {
    final Map<? super K, ? extends V> resolved = map.get(scope);
    final V value = resolved.get(key);
    if (value == null && !resolved.containsKey(key)) {
      throw new NoSuchElementException(
        String.format("No entry for key %s.", key)
      );
    }
    return value;
  }
}
