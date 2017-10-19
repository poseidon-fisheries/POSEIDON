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

/**
 * A simple strategy for departure where the fisher decides to get out of port at random with fixed probability
 * Created by carrknight on 4/18/15.
 */
public class FixedProbabilityDepartingStrategy implements DepartingStrategy {

    private final double probabilityToLeavePort;


    private double dayLastCheck = -1;

    final private boolean checkOnlyOnceADay;

    public FixedProbabilityDepartingStrategy(double probabilityToLeavePort, boolean checkOnlyOnceADay)
    {
        Preconditions.checkArgument(probabilityToLeavePort >= 0, "Probability can't be negative!");
        Preconditions.checkArgument(probabilityToLeavePort <= 1, "Probability can't be above 1");
        this.probabilityToLeavePort = probabilityToLeavePort;
        this.checkOnlyOnceADay = checkOnlyOnceADay;
    }


    /**
     * ignored
     */
    @Override
    public void start(FishState model,Fisher fisher) {

    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     *  @param fisher
     * @param model the model. Not used  @return true if the fisherman wants to leave port.
     * @param random */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        if(checkOnlyOnceADay && model.getDay()==dayLastCheck) //if you already checked don't bother
            return false;
        dayLastCheck = model.getDay();
        return random.nextBoolean(probabilityToLeavePort);
    }


    public double getProbabilityToLeavePort() {
        return probabilityToLeavePort;
    }


    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        //nothing
    }
}

