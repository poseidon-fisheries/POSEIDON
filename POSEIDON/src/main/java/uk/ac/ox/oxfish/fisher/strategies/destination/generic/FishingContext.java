/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.destination.generic;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * the context object containing a link to all important things related to an observed trip
 */
public class FishingContext {

    /**
     * the person who is observing the fishing
     */
    private final Fisher observer;

    /**
     * the person who actually did the trip
     */
    private final Fisher actor;

    /**
     * the fish state object
     */
    private final FishState state;

    /**
     * the randomizer
     */
    private final MersenneTwisterFast random;

    public FishingContext(Fisher observer, Fisher actor, FishState state, MersenneTwisterFast random) {
        this.observer = observer;
        this.actor = actor;
        this.state = state;
        this.random = random;
    }

    /**
     * Getter for property 'observer'.
     *
     * @return Value for property 'observer'.
     */
    public Fisher getObserver() {
        return observer;
    }

    /**
     * Getter for property 'actor'.
     *
     * @return Value for property 'actor'.
     */
    public Fisher getActor() {
        return actor;
    }

    /**
     * Getter for property 'state'.
     *
     * @return Value for property 'state'.
     */
    public FishState getState() {
        return state;
    }

    /**
     * Getter for property 'random'.
     *
     * @return Value for property 'random'.
     */
    public MersenneTwisterFast getRandom() {
        return random;
    }
}
