/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.registers;

import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

public interface Register<T> {

    Optional<T> get(Vessel vessel);

    Stream<Entry<Vessel, T>> getAllEntries();

    default Stream<Entry<Vessel, T>> getOtherEntries(final Vessel vessel) {
        return getAllEntries().filter(entry -> !entry.getKey().equals(vessel));
    }
    
}
