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

package uk.ac.ox.poseidon.agents.travel;

import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.math.RoundingMode.HALF_EVEN;

/**
 * The cost of the time a vessel would spend sailing a {@link Route}: its hourly cost times the
 * route's hours. The hourly cost is read at each call.
 */
public class TimeCost implements Function<Route, Money> {

    private final Vessel vessel;
    private final Function<? super Vessel, ? extends Money> hourlyCost;

    /**
     * @param vessel     the vessel that would sail the route
     * @param hourlyCost gives the vessel's cost per hour at sea
     */
    TimeCost(
        final Vessel vessel,
        final Function<? super Vessel, ? extends Money> hourlyCost
    ) {
        this.vessel = checkNotNull(vessel);
        this.hourlyCost = checkNotNull(hourlyCost);
    }

    /**
     * @param route the route the vessel would sail
     * @return the vessel's hourly cost times the hours {@code route} takes to sail
     * @throws NullPointerException if {@code hourlyCost} gives no cost for the vessel
     */
    @Override
    public Money apply(final Route route) {
        return checkNotNull(
            hourlyCost.apply(vessel),
            "Unable to extract hourly cost for Vessel %s.",
            vessel
        ).multipliedBy(route.getDuration().toSeconds() / 3600.0, HALF_EVEN);
    }
}
