/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.registers;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class DynamicRegister<T> implements Register<T> {

    private final WeakHashMap<Vessel, T> map = new WeakHashMap<>();
    private final Function<Vessel, T> mappingFunction;

    @Override
    public Optional<T> get(final Vessel vessel) {
        return Optional.ofNullable(map.computeIfAbsent(vessel, mappingFunction));
    }

    @Override
    public Stream<Vessel> getVessels() {
        return map.keySet().stream();
    }

    @Override
    public Stream<Map.Entry<Vessel, T>> getAllEntries() {
        return map.entrySet().stream();
    }

}
