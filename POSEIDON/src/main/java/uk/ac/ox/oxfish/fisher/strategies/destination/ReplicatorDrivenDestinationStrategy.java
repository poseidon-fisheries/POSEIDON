/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A very simple pair of "index" and delegate that represents a destination strategy
 * that is being forced from the outside through a strategy replicator
 * Created by carrknight on 2/28/17.
 */
public class ReplicatorDrivenDestinationStrategy implements DestinationStrategy {


    private final int strategyIndex;


    private final DestinationStrategy delegate;


    public ReplicatorDrivenDestinationStrategy(
        int strategyIndex, DestinationStrategy delegate
    ) {
        this.strategyIndex = strategyIndex;
        this.delegate = delegate;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
        Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction
    ) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }

    /**
     * Getter for property 'strategyIndex'.
     *
     * @return Value for property 'strategyIndex'.
     */
    public int getStrategyIndex() {
        return strategyIndex;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public DestinationStrategy getDelegate() {
        return delegate;
    }
}
