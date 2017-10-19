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

import com.google.common.annotations.VisibleForTesting;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The Fisher keep fishing until full or until a limit number of days have passed.
 * Created by carrknight on 6/23/15.
 */
public class MaximumDaysDecorator implements FishingStrategy
{


    private final FishingStrategy delegate;

    private final int daysBeforeGoingHome;

    public MaximumDaysDecorator(int daysBeforeGoingHome) {
        this(new FishUntilFullStrategy(1d),daysBeforeGoingHome);


    }

    public MaximumDaysDecorator(FishingStrategy delegate, int daysBeforeGoingHome) {
        this.delegate = delegate;
        this.daysBeforeGoingHome = daysBeforeGoingHome;
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     *
     * @param random the randomizer
     * @param model  the model itself   @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return
                 fisher.getHoursAtSea() /24d  <= daysBeforeGoingHome && delegate.shouldFish(fisher,random,model,currentTrip) ;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);
    }

    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }


    @VisibleForTesting
    public  FishingStrategy accessDecorated(){
        return delegate;
    }
}
