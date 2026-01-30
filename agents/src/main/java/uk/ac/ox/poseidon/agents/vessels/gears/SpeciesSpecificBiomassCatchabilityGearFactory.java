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
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.agents.utils.SpeciesSpecificRateFactorySupport;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.file.Path;
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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesSpecificBiomassCatchabilityGearFactory<S extends Scope>
    extends RelativeScopeFactory<S, SpeciesSpecificBiomassCatchabilityGear> {

    private String code;
    private Factory<? super S, ? extends Supplier<Duration>> durationSupplier;
    private Factory<? super S, ? extends Collection<? extends Species>> species;

    @Singular
    private Map<String, Double> proportions;

    @Override
    protected SpeciesSpecificBiomassCatchabilityGear newInstance(final S scope) {
        checkNotNull(proportions, "proportions should not be null");
        final HashSet<Species> species = new HashSet<>(this.species.get(scope));
        final SpeciesIndex speciesIndex = SpeciesIndex.of(species);
        final Map<String, Species> speciesByKey =
            species.stream().collect(java.util.stream.Collectors.toMap(
                Species::getKey,
                identity()
            ));

        final double[] proportionArray = new double[speciesIndex.size()];
        proportions.forEach((speciesKey, proportion) -> {
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

    public static <S extends Scope> SpeciesSpecificBiomassCatchabilityGearFactory<S> fromFile(
        final Path speciesFilePath,
        final String codeColumnName,
        final String lifeStageColumnName,
        final String code,
        final Factory<? super S, ? extends Supplier<Duration>> durationSupplier,
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final double defaultProportion
    ) {
        checkUnitRange(defaultProportion, "defaultProportion");
        final Map<String, Double> proportions =
            SpeciesSpecificRateFactorySupport.readRatesByKeyFromFile(
                speciesFilePath,
                codeColumnName,
                lifeStageColumnName,
                defaultProportion
            );
        return new SpeciesSpecificBiomassCatchabilityGearFactory<>(
            code,
            durationSupplier,
            species,
            proportions
        );
    }
}
