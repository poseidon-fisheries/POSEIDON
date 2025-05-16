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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ImmutableRegister<T> implements Register<T> {

    private final ImmutableMap<Vessel, T> map;

    public ImmutableRegister(final Map<Vessel, T> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<T> get(final Vessel vessel) {
        return Optional.ofNullable(map.get(vessel));
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
