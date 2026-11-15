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

import java.util.function.BiConsumer;

/**
 * Read-only entries, visited in the order their keys were first observed.
 * Recording an existing key does not move it. A visit must not record into
 * the entries it visits.
 * @param <K> the entry key type
 * @param <M> the remembered type
 */
public interface MemoryEntries<K, M> {
  /** @param action called once for each key and its remembered value */
  void forEachEntry(BiConsumer<? super K, ? super M> action);
}
