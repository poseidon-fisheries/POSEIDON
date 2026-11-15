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

import static lombok.AccessLevel.PACKAGE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * A memory with independent entries for each partition, selected from its
 * input. Switching back to a partition gives its original entries again.
 * A null partition key is a key like any other.
 * @param <T> the input type
 * @param <P> the partition key type
 * @param <K> the entry key type
 * @param <M> the remembered type
 */
@RequiredArgsConstructor(access = PACKAGE)
public class PartitionedMemory<
  T,
  P,
  K,
  M
> implements Function<T, MutableMemoryEntries<K, M>> {

  private final Function<? super T, ? extends P> partitionKey;
  private final Map<P, MutableMemoryEntries<K, M>> partitions = new HashMap<>();

  @Override
  public MutableMemoryEntries<K, M> apply(final T input) {
    return partitions.computeIfAbsent(partitionKey.apply(input), _ ->
      new MutableMemoryEntries<>()
    );
  }
}
