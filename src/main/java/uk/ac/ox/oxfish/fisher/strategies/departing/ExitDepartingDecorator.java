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
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import java.util.Iterator;

/**
 * if the fisher made losses on  average in the last x consecutive years, he's out
 */
public class ExitDepartingDecorator implements DepartingStrategy {


    private final DepartingStrategy decorated;

    private boolean hasQuit =false;

    private final int consecutiveYearsNegative;

    private Stoppable stoppable;


    public ExitDepartingDecorator(DepartingStrategy decorated, int consecutiveYearsNegative)
    {
        this.decorated = decorated;
        this.consecutiveYearsNegative = consecutiveYearsNegative;
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        if(hasQuit)
            return false;
        else
            return decorated.shouldFisherLeavePort(fisher, model, random);
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        //shedule yourself to check for profits every year
        if(stoppable != null)
            throw  new RuntimeException("Already started!");

        Steppable steppable = new Steppable() {
            @Override
            public void step(SimState simState) {
                checkIfQuit(fisher);
            }
        };
        this.stoppable = model.scheduleEveryYear(steppable, StepOrder.DAWN);
        decorated.start(model,fisher);

    }

    @Override
    public void turnOff(Fisher fisher) {

        Preconditions.checkArgument(stoppable != null, "Can't turn off ");
        this.stoppable.stop();
        decorated.turnOff(fisher);
    }




    public void checkIfQuit(Fisher fisher){
        if(!hasQuit) {
            DataColumn earningsData = fisher.getYearlyData().getColumn(FisherYearlyTimeSeries.EARNINGS);
            DataColumn costData = fisher.getYearlyData().getColumn(FisherYearlyTimeSeries.VARIABLE_COSTS);
            if(earningsData.size() >= consecutiveYearsNegative) {
                Iterator<Double> earningsIterator = earningsData.descendingIterator();
                Iterator<Double> costsIterator = costData.descendingIterator();
                double sum = 0;
                for(int i=0; i<consecutiveYearsNegative; i++) {
                    sum+=earningsIterator.next();
                    sum-=costsIterator.next();
                }
                //you are here, all your profits were negative!
                if(sum<0)
                    hasQuit = true;
            }
        }
    }

    @Override
    public int predictedDaysLeftFishingThisYear(Fisher fisher, FishState model, MersenneTwisterFast random) {
        if(hasQuit)
            return 0;
        else
            return decorated.predictedDaysLeftFishingThisYear(fisher, model, random);
    }
}
