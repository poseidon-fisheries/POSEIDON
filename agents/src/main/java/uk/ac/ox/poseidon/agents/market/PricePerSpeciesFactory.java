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

package uk.ac.ox.poseidon.agents.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricePerSpeciesFactory extends GlobalScopeFactory<Map<Species, Price>> {

    private Factory<? extends List<Species>> speciesList;
    private Map<String, Factory<? extends Price>> pricePerSpeciesCode;

    @Override
    protected Map<Species, Price> newInstance(final Simulation simulation) {
        checkNotNull(speciesList, "speciesList must not be null");
        checkNotNull(pricePerSpeciesCode, "pricePerSpeciesCode must not be null");
        final Map<String, Species> speciesByCode =
            speciesList.get(simulation).stream().collect(toMap(Species::getCode, identity()));
        return pricePerSpeciesCode.entrySet().stream().collect(toImmutableMap(
            entry -> speciesByCode.get(entry.getKey()),
            entry -> entry.getValue().get(simulation)
        ));
    }

}
