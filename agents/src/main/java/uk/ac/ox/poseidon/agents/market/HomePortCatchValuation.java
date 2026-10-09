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

import lombok.RequiredArgsConstructor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.catches.CatchCategoriser;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.buckets.Bucket;

import java.util.Map;
import java.util.function.ToDoubleBiFunction;

import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PACKAGE;

/**
 * Values a catch at the prices of the market at a vessel's home port, as it would sell there now:
 * the catch is sorted by the vessel's own {@link CatchCategoriser}, priced by
 * {@link Market#quote}, and summed by {@link Sale#summarise}. Species without a price count as
 * zero. The option the catch is remembered for is ignored, so that this can be combined with
 * other valuations of the same shape. Everything is read at each call, so a change of home port
 * or of prices shows straight away. Assumes a single currency.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class HomePortCatchValuation implements ToDoubleBiFunction<Object, Bucket> {

    private final Vessel vessel;
    private final CatchCategoriser catchCategoriser;

    /**
     * @throws IllegalStateException if the vessel has no home port, its home port does not have
     *                               exactly one market, or the catch is priced in more than one
     *                               currency
     */
    @Override
    public double applyAsDouble(
        final Object option,
        final Bucket bucket
    ) {
        checkState(vessel.getHomePort() != null, "Vessel %s has no home port.", vessel.getId());
        final Market market = vessel.getMarketGrid().getMarket(vessel.getHomePort());
        final Map<CurrencyUnit, Money> summary =
            Sale.summarise(market.quote(catchCategoriser.apply(bucket)));
        checkState(
            summary.size() <= 1,
            "Catch of vessel %s priced in more than one currency: %s.",
            vessel.getId(),
            summary.keySet()
        );
        return summary.values().stream()
            .findAny()
            .map(money -> money.getAmount().doubleValue())
            .orElse(0.0);
    }
}
