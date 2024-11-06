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

package uk.ac.ox.poseidon.biology.species;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

@EqualsAndHashCode
class ImmutableSpeciesList implements SpeciesList {
    @Getter
    private final List<Species> allSpecies;
    private final ImmutableMap<String, Species> speciesByCode;
    private final ImmutableMap<String, Species> speciesByName;

    ImmutableSpeciesList(final Collection<Species> species) {
        this.allSpecies =
            species
                .stream()
                .distinct()
                .collect(toImmutableList());
        this.speciesByCode =
            allSpecies.stream().collect(toImmutableMap(
                Species::getCode,
                identity()
            ));
        this.speciesByName =
            allSpecies.stream().collect(toImmutableMap(
                Species::getName,
                identity()
            ));
    }

    @Override
    public Species getByName(final String speciesName) {
        return speciesByName.get(speciesName);
    }

    @Override
    public Species getByCode(final String speciesCode) {
        return speciesByCode.get(speciesCode);
    }
}
