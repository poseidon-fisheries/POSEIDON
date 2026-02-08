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

package uk.ac.ox.poseidon.core;

import lombok.NonNull;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.ConstantFactory;

import java.util.List;
import java.util.stream.Stream;

public interface Factory<S extends Scope, C> {
    C get(S scope);

    static <C> ConstantFactory<C> of(@NonNull final C value) {
        return new ConstantFactory<>(value);
    }

    static <C> ConstantFactory<List<C>> of(@NonNull final Stream<C> values) {
        return new ConstantFactory<>(values.toList());
    }

    @SafeVarargs
    static <C> ConstantFactory<List<C>> of(
        @NonNull final C value,
        @NonNull final C... values
    ) {
        return new ConstantFactory<>(
            Stream.concat(Stream.of(value), Stream.of(values)).toList()
        );
    }
}
