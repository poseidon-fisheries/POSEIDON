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

import lombok.*;
import org.apache.commons.collections4.keyvalue.MultiKey;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.function.UnaryOperator.identity;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesSpecificBiomassCatchabilityGearFactory<S extends Scope>
    extends RelativeScopeFactory<S, SpeciesSpecificBiomassCatchabilityGear> {

    private String code;
    private Factory<? super S, ? extends Supplier<Duration>> durationSupplier;
    private Factory<? super S, ? extends Collection<? extends Species>> species;

    private Factory<? super S, ? extends Map<MultiKey<Object>, Double>> proportions;

    @Override
    protected SpeciesSpecificBiomassCatchabilityGear newInstance(final S scope) {
        final Map<MultiKey<Object>, Double> resolvedProportions = proportions.get(scope);
        checkNotNull(resolvedProportions, "proportions should not be null");
        final HashSet<Species> species = new HashSet<>(this.species.get(scope));
        final SpeciesIndex speciesIndex = SpeciesIndex.of(species);
        final Map<MultiKey<Object>, Species> speciesByKey =
            species.stream().collect(java.util.stream.Collectors.toMap(
                Species::getKey,
                identity()
            ));

        final double[] proportionArray = new double[speciesIndex.size()];
        resolvedProportions.forEach((speciesKey, proportion) -> {
            final Species resolvedSpecies = speciesByKey.get(speciesKey);
            checkArgument(
                resolvedSpecies != null,
                "Unknown speciesKey '%s' in proportionBySpeciesKey",
                speciesKey
            );
            proportionArray[speciesIndex.indexOf(resolvedSpecies)] =
                checkUnitRange(proportion, "proportion");
        });

        return new SpeciesSpecificBiomassCatchabilityGear(
            code,
            speciesIndex,
            proportionArray,
            durationSupplier.get(scope)
        );
    }

}
