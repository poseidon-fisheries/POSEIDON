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

package uk.ac.ox.poseidon.fishing.choices;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory;

/**
 * A {@link VesselScopeFactory} counterpart of {@link KeyedMemorySelector},
 * built via {@link Factories#catchMemoryBy(ValueFactory)}. Each selector it
 * builds selects from a new, empty {@link KeyedMemory}.
 * <p>
 * A memory is what its vessel knows across components, so it is
 * vessel-level: each vessel has its own, and everything given this factory
 * object, whether it remembers or reads, reaches the same one.
 *
 * @param <T> the type of input the key is read from
 * @param <K> the type of key
 * @param <O> the type of option observed
 * @param <M> the type of what is remembered about an option
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class KeyedMemorySelectorFactory<
  T,
  K,
  O,
  M
> extends VesselScopeFactory<KeyedMemorySelector<T, K, O, M>> {

  private final ValueFactory<? super VesselScope, ? super T, ? extends K> key;

  @Override
  protected KeyedMemorySelector<T, K, O, M> newInstance(
    final VesselScope scope
  ) {
    return new KeyedMemorySelector<>(new KeyedMemory<>(), key.get(scope));
  }
}
