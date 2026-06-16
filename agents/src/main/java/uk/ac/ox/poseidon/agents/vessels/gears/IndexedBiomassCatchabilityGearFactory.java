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

package uk.ac.ox.poseidon.agents.vessels.gears;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IndexedBiomassCatchabilityGearFactory<S extends Scope>
    extends RelativeScopeFactory<S, IndexedBiomassCatchabilityGear> {

    private String code;
    private Factory<? super S, ? extends Supplier<Duration>> durationSupplier;
    private Factory<? super S, ? extends Collection<? extends Species>> species;

    private Factory<? super S, ? extends Function<? super Species, Double>> proportionFunction;

    @Override
    protected IndexedBiomassCatchabilityGear newInstance(final S scope) {
        final var function = proportionFunction.get(scope);
        return new IndexedBiomassCatchabilityGear(
            code,
            SpeciesIndex.of(species.get(scope))
                .mapToDoubleArray(s -> checkUnitRange(function.apply(s), "proportion")),
            durationSupplier.get(scope)
        );
    }
}
