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

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PACKAGE;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.NoArgsConstructor;

/**
 * Entries updated by recorder-supplied rules. Every observation, including
 * the first, reaches its rule with the observation's time. A null rule result
 * is rejected without replacing the entry: forgetting is not an update.
 * @param <K> the entry key type
 * @param <M> the remembered type
 */
@NoArgsConstructor(access = PACKAGE)
public class MutableMemoryEntries<K, M> implements MemoryEntries<K, M> {

  private final Map<K, M> contents = new LinkedHashMap<>();

  /**
   * @param key the key to record under
   * @param observation what was observed
   * @param observationDateTime when it was observed
   * @param rule how to update what is remembered
   * @param <T> the observation type, which may differ from the remembered type
   */
  public <T> void record(
    final K key,
    final T observation,
    final LocalDateTime observationDateTime,
    final UpdateRule<? super T, M> rule
  ) {
    contents.put(
      key,
      requireNonNull(
        rule.update(
          Optional.ofNullable(contents.get(key)),
          requireNonNull(observation, "observation"),
          requireNonNull(observationDateTime, "observationDateTime")
        ),
        "rule result"
      )
    );
  }

  @Override
  public void forEachEntry(final BiConsumer<? super K, ? super M> action) {
    contents.forEach(action);
  }
}
