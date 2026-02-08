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
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.catches.CatchCategory;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.List;
import java.util.Map;

import static uk.ac.ox.poseidon.agents.market.PriceEntry.groupByCategoryAndSpecies;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OneBiomassMarketPerPortFactory
    extends SimulationScopeFactory<List<BiomassMarket>> {

    private Factory<? super SimulationScope, ? extends PortGrid> portGrid;
    private Factory<? super SimulationScope, ? extends List<PriceEntry>> pricesEntries;

    @Override
    protected List<BiomassMarket> newInstance(final SimulationScope scope) {
        final Map<CatchCategory, Map<Species, Price>> prices =
            groupByCategoryAndSpecies(pricesEntries.get(scope));
        final EventManager eventManager = scope.getSimulation().getEventManager();
        return portGrid.get(scope).getPorts()
            .map(port ->
                new BiomassMarket(
                    port,
                    port.getCode(),
                    prices,
                    eventManager
                )
            )
            .toList();
    }
}
