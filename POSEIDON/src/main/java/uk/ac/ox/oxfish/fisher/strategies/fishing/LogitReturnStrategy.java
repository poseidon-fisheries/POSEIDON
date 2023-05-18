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
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Use a logit classifier to judge whether or not to return to port.
 * It's key to remember that the classifier returns TRUE if it wants to RETURN (that is, it returns the opposite
 * boolean value compared to the question we are asking)
 * Created by carrknight on 4/19/17.
 */
public class LogitReturnStrategy implements FishingStrategy {


    /**
     * logit classifier to ask if we should go back
     */
    private final LogisticClassifier shouldIReturnClassifier;


    public LogitReturnStrategy(
        LogisticClassifier shouldIReturnClassifier
    ) {
        this.shouldIReturnClassifier = shouldIReturnClassifier;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        //ignored

    }

    @Override
    public void turnOff(Fisher fisher) {
        //ignored
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
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {

        //do not return if you haven't done any fishing yet
        if (currentTrip.getEffort() == 0)
            return true;
        //always return if full
        if (fisher.getTotalWeightOfCatchInHold() / fisher.getMaximumHold() >= 1d - FishStateUtilities.EPSILON)
            return false;
        //otherwise check if you want to continue by calling the logit
        //should I fish is the inverse of should I return
        return !shouldIReturnClassifier.test(fisher, model, fisher.getLocation(), random);


    }

    /**
     * Getter for property 'classifier'.
     *
     * @return Value for property 'classifier'.
     */
    public LogisticClassifier getShouldIReturnClassifier() {
        return shouldIReturnClassifier;
    }
}
