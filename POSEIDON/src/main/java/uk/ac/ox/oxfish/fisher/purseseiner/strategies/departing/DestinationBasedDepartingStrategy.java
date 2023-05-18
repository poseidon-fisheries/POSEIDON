/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A departing strategy that relies on a destination strategy: it will say we should leave port only
 * if the destination strategy thinks we should go somewhere else than the port.
 * <p>Will only work if the destination strategy does not rely on the {@code currentAction}
 * argument, as we pass {@code * null} into it.
 */
public class DestinationBasedDepartingStrategy implements DepartingStrategy {

    private DestinationStrategy destinationStrategy;

    public DestinationStrategy getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(final DestinationStrategy destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    @Override
    public boolean shouldFisherLeavePort(
        final Fisher fisher,
        final FishState fishState,
        final MersenneTwisterFast rng
    ) {
        checkNotNull(destinationStrategy);
        final SeaTile destination =
            destinationStrategy.chooseDestination(fisher, rng, fishState, null);
        return destination != fisher.getHomePort().getLocation();
    }
}
