/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.utils;

import lombok.*;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListFactory<S extends Scope, C> extends RelativeScopeFactory<S, List<C>> {

    @Singular
    private List<Factory<? super S, ? extends C>> factories;

    @Override
    protected List<C> newInstance(final S scope) {
        return factories
            .stream()
            .map(f -> (C) f.get(scope))
            .toList();
    }

    @SafeVarargs
    public static <S extends Scope, C> ListFactory<S, C> from(
        final Factory<? super S, ? extends C>... factories
    ) {
        final List<Factory<? super S, ? extends C>> list = List.of(factories);
        return new ListFactory<>(list);
    }
}
