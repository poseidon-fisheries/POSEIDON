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

package uk.ac.ox.poseidon.agents.catches.disposition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.agents.utils.SpeciesSpecificRateFactorySupport;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesSpecificDiscardMortalityRatesFactory<S extends Scope>
    extends RelativeScopeFactory<S, SpeciesSpecificDiscardMortalityRates> {

    private Factory<? super S, ? extends Collection<? extends Species>> species;

    @Singular("mortalityRateBySpeciesKey")
    private Map<String, Double> mortalityRatesBySpeciesKey;

    @Override
    protected SpeciesSpecificDiscardMortalityRates newInstance(final S scope) {
        final var rates = SpeciesSpecificRateFactorySupport.buildRatesByKey(
            species.get(scope),
            mortalityRatesBySpeciesKey,
            "mortality rate"
        );
        return new SpeciesSpecificDiscardMortalityRates(rates);
    }

    public static <S extends Scope> SpeciesSpecificDiscardMortalityRatesFactory<S> fromFile(
        final Path speciesFilePath,
        final String codeColumnName,
        final String lifeStageColumnName,
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final double defaultRate
    ) {
        final Map<String, Double> rates =
            SpeciesSpecificRateFactorySupport.readRatesByKeyFromFile(
                speciesFilePath,
                codeColumnName,
                lifeStageColumnName,
                defaultRate
            );
        return new SpeciesSpecificDiscardMortalityRatesFactory<>(
            species,
            rates
        );
    }
}
