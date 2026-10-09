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
import uk.ac.ox.poseidon.agents.tasks.ExtendedTripTask;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.util.function.Supplier;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkState;

/**
 * Sells the vessel's hold contents to the market of its home port, where it must be, and credits
 * the trip's account with the proceeds. The market is found by port, not by cell, because ports
 * can share a cell.
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
     * Sells the vessel's entire hold to the market of its home port, adding the sale's proceeds
     * (by currency) to the trip's account.
     *
     * @return {@link Status#SUCCEEDED}
     * @throws IllegalStateException if the vessel has no home port, is not at its home port, or
     *                               its home port does not have exactly one market
     */
    @Override
    protected Status complete() {
        final Vessel vessel = getAgent();
        checkState(vessel.getHomePort() != null, "Vessel %s has no home port.", vessel.getId());
        checkState(
            vessel.getCell().equals(vessel.getHomePortLocation()),
            "Vessel %s is landing at %s, not at its home port %s.",
            vessel.getId(),
            vessel.getCell(),
            vessel.getHomePort()
        );
        vessel
            .getMarketGrid()
            .getMarket(vessel.getHomePort())
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
