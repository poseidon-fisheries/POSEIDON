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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The fisher keeps fishing until the percentage of hold filled is above a threshold.
 * <p>
 * Created by carrknight on 5/5/15.
 */
public class FishUntilFullStrategy implements FishingStrategy {

    private final static double EPSILON = .001;
    private double minimumPercentageFull;

    public FishUntilFullStrategy(double minimumPercentageFull) {
        this.minimumPercentageFull = minimumPercentageFull;
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param random the randomizer
     * @param model  the model itself   @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {
        return fisher.getTotalWeightOfCatchInHold() + EPSILON <
            fisher.getMaximumHold() * minimumPercentageFull;
    }

    public double getMinimumPercentageFull() {
        return minimumPercentageFull;
    }

    public void setMinimumPercentageFull(double minimumPercentageFull) {
        this.minimumPercentageFull = minimumPercentageFull;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        //ignored
    }

    /**
     * ignored
     *
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }
}



