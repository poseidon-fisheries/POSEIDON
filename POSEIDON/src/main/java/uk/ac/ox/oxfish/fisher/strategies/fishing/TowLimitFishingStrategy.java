/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This should be turned into a regulation at some point, but this represents
 * a fisher who stops towing after doing it x times
 * Created by carrknight on 6/21/17.
 */
public class TowLimitFishingStrategy implements FishingStrategy {

    private final int maxNumberOfTows;

    public TowLimitFishingStrategy(int maxNumberOfTows) {
        this.maxNumberOfTows = maxNumberOfTows;
    }

    private final FishUntilFullStrategy delegate = new FishUntilFullStrategy(1.0);


    @Override
    public void start(FishState model, Fisher fisher)
    {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param fisher
     * @param random      the randomizer
     * @param model       the model itself
     * @param currentTrip
     * @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return delegate.shouldFish(fisher, random, model, currentTrip) &&
                currentTrip.getEffort()<=maxNumberOfTows;
    }

    /**
     * Getter for property 'maxNumberOfTows'.
     *
     * @return Value for property 'maxNumberOfTows'.
     */
    public int getMaxNumberOfTows() {
        return maxNumberOfTows;
    }


}

