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
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A {@link RelativeScopeFactory} for the {@link MarketPrice}s of one market: one per entry in
 * {@link #priceEntries}, all at {@link #market}. Built via {@link Factories#marketPrices}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MarketPricesFactory<S extends Scope>
    extends RelativeScopeFactory<S, List<MarketPrice>> {

    private Factory<? super S, ? extends BiomassMarket> market;
    private Factory<? super S, ? extends List<PriceEntry>> priceEntries;

    @Override
    protected List<MarketPrice> newInstance(final S scope) {
        final BiomassMarket market = this.market.get(scope);
        return priceEntries
            .get(scope)
            .stream()
            .map(priceEntry -> new MarketPrice(market, priceEntry))
            .collect(toImmutableList());
    }
}
