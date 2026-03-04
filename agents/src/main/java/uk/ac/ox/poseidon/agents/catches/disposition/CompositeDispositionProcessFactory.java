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

package uk.ac.ox.poseidon.agents.catches.disposition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompositeDispositionProcessFactory<S extends Scope>
    extends RelativeScopeFactory<S, CompositeDispositionProcess> {

    private List<Factory<? super S, ? extends DispositionProcess>> dispositionStrategies;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public CompositeDispositionProcessFactory(
        final Factory<? super S, ? extends DispositionProcess>... dispositionStrategies
    ) {
        this(List.of(dispositionStrategies));
    }

    @Override
    protected CompositeDispositionProcess newInstance(final S scope) {
        return new CompositeDispositionProcess(
            dispositionStrategies
                .stream()
                .map(f -> f.get(scope))
                .collect(toImmutableList())
        );
    }

}
