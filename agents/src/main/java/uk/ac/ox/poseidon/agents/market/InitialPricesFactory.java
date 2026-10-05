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

package uk.ac.ox.poseidon.agents.market;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A {@link SimulationScopeFactory} that applies the resolved {@link MarketPrice}s as soon as it is
 * built, so that markets have them from the start of the simulation, and returns them. Prices are
 * applied when the simulation is built rather than scheduled at its start, so that prices set
 * before the first step, e.g. by a SURIMI {@code UpdateSpeciesPrices} message, are not
 * overwritten. No separate plain component class here: applying the prices is a side effect of
 * building the factory. Built via {@link Factories#initialPrices}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InitialPricesFactory extends SimulationScopeFactory<List<MarketPrice>> {

    private List<Factory<? super SimulationScope, ? extends List<MarketPrice>>> marketPrices;

    @Override
    protected List<MarketPrice> newInstance(final SimulationScope scope) {
        final List<MarketPrice> resolved = marketPrices
            .stream()
            .flatMap(factory -> factory.get(scope).stream())
            .collect(toImmutableList());
        resolved.forEach(MarketPrice::apply);
        return resolved;
    }
}
