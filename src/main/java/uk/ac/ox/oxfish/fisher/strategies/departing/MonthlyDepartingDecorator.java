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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Season;

/**
 * The fisher is willing to go out only some months of the year
 * Created by carrknight on 1/6/16.
 */
public class MonthlyDepartingDecorator implements  DepartingStrategy {


    private final boolean allowedAtSea[];

    private final DepartingStrategy delegate;


    public MonthlyDepartingDecorator(
            DepartingStrategy delegate, boolean[] allowedAtSea) {
        this.delegate = delegate;
        Preconditions.checkArgument(allowedAtSea.length == 12);
        this.allowedAtSea = allowedAtSea;
    }

    public MonthlyDepartingDecorator(
            DepartingStrategy delegate, int... monthsAllowed) {
        this.delegate = delegate;

        allowedAtSea = new boolean[12];
        for(int month : monthsAllowed)
            allowedAtSea[month]=true;

    }

    /**
     * The fisher goes out only on allotted months
     *
     *
     * @param fisher the fisher making the decision
     * @param model the state
     * @param random the randomizer
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        //integer division, gets you the "month" correctly
        int month = Season.getMonth(model.getDayOfTheYear())-1;
        assert month>=0;
        assert month<=11;
        return allowedAtSea[month] & delegate.shouldFisherLeavePort(fisher, model, random);

    }

    @Override
    public void start(FishState model, Fisher fisher) {
        //doesn't schedule itself
    }

    @Override
    public void turnOff(Fisher fisher) {
        //doesn't need to turn off
    }

    public boolean[] getAllowedAtSea() {
        return allowedAtSea;
    }
}
