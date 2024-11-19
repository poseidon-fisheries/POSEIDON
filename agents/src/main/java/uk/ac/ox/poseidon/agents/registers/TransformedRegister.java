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

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.entry;

@RequiredArgsConstructor
public class TransformedRegister<S, T> implements Register<T> {

    private final Register<S> sourceRegister;
    private final Function<Stream<Entry<Vessel, S>>, Stream<Entry<Vessel, T>>> transformer;

    @Override
    public Optional<T> get(final Vessel vessel) {
        final Optional<Entry<Vessel, S>> sourceEntry =
            sourceRegister
                .get(vessel)
                .map(value -> entry(vessel, value));
        return transformer
            .apply(sourceEntry.stream())
            .findAny()
            .map(Entry::getValue);
    }

    @Override
    public Stream<Entry<Vessel, T>> getAllEntries() {
        return transformer.apply(sourceRegister.getAllEntries());
    }

}
