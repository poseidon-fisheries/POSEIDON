/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

public class MaximumDaysAYearDecorator implements FishingStrategy {


    private final FishingStrategy delegate;


    private final int maxNumberOfDaysOutPerYear;


    public MaximumDaysAYearDecorator(FishingStrategy delegate, int maxNumberOfDaysOutPerYear) {
        this.delegate = delegate;
        this.maxNumberOfDaysOutPerYear = maxNumberOfDaysOutPerYear;
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
        Fisher fisher, MersenneTwisterFast random, FishState model,
        TripRecord currentTrip
    ) {

        if (fisher.getHoursAtSeaThisYear() / 24 > maxNumberOfDaysOutPerYear)
            return false;

        return delegate.shouldFish(fisher, random, model, currentTrip);
    }

    /**
     * This is called by Arriving.act to decide whether or not to fish up arrival. Most fishing
     * strategies should use this default implementation, but FAD fishing strategies are expected to
     * override this method and result in action types other than `Fishing`.
     *
     * @param model
     * @param agent
     * @param regulation
     * @param hoursLeft
     */
    @Override
    public ActionResult act(
        FishState model, Fisher agent,
        Regulation regulation, double hoursLeft
    ) {
        return delegate.act(model, agent, regulation, hoursLeft);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }
}
