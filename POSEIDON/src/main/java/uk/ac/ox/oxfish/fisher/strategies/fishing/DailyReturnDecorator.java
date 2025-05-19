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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import com.google.common.annotations.VisibleForTesting;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * A fishing strategy decorator that only allows 1 check a day (and no check before 24 hours have passed or the first tow hasn't completed)
 * Created by carrknight on 4/20/17.
 */
public class DailyReturnDecorator implements FishingStrategy {


    private final FishingStrategy decorated;
    private double lastCheck = -1000;


    public DailyReturnDecorator(FishingStrategy decorated) {
        this.decorated = decorated;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        this.decorated.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        this.decorated.turnOff(fisher);
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

        //always stop if you are full
        if (fisher.getTotalWeightOfCatchInHold() >= fisher.getMaximumHold() - FishStateUtilities.EPSILON) {
            lastCheck = -1000;
            return false;
        }
        //fish at least once and at least for a day!
        if (fisher.getHoursAtSea() < 24 || currentTrip.getEffort() == 0)
            return true;
        //if you checked recently, don't check again
        if (fisher.getHoursAtSea() < lastCheck + 24)
            return true;

        //check!
        if (decorated.shouldFish(fisher, random, model, currentTrip)) {
            lastCheck = fisher.getHoursAtSea();
            return true;
        } else {
            lastCheck = -1000;
            return false;
        }
    }

    /**
     * Getter for property 'lastCheck'.
     *
     * @return Value for property 'lastCheck'.
     */
    @VisibleForTesting
    public double getLastCheck() {
        return lastCheck;
    }

    @VisibleForTesting
    public FishingStrategy accessDecorated() {
        return decorated;
    }
}
