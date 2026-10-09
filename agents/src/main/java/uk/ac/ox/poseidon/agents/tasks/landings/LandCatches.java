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

package uk.ac.ox.poseidon.agents.tasks.landings;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.market.Market;
import uk.ac.ox.poseidon.agents.tasks.ExtendedTripTask;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkState;

/**
 * Sells the vessel's hold contents to a market at its current cell and credits the trip's account
 * with the proceeds.
 */
@RequiredArgsConstructor
public class LandCatches extends ExtendedTripTask {

    private final Supplier<Duration> durationSupplier;

    /** @return how long landing takes */
    @Override
    protected Duration getDuration() {
        return durationSupplier.get();
    }

    /**
     * Sells the vessel's entire hold to a market at its current cell, adding the sale's proceeds
     * (by currency) to the trip's account.
     *
     * @return {@link Status#SUCCEEDED}
     * @throws IllegalStateException if there is not exactly one market at the vessel's current
     *                               cell
     */
    @Override
    protected Status complete() {
        final Vessel vessel = getAgent();
        final List<Market> markets = vessel
            .getMarketGrid()
            .getObjectsAt(vessel.getCell())
            .toList();
        checkState(
            markets.size() == 1,
            "Expected one market for vessel %s at location %s, found %s.",
            vessel.getId(),
            vessel.getCell(),
            markets.size()
        );
        final Market market = markets.getFirst();
        market
            .sell(
                vessel,
                vessel.getHold().extractContent(),
                vessel.getSchedule().getDateTime()
            )
            .summary()
            .values()
            .forEach(getTrip().getAccount()::add);
        return SUCCEEDED;
    }

}
