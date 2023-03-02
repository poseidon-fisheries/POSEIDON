/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.MTFApache;
import org.apache.commons.math3.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * planning module that uses discrete sampling to pick the next option and where the probability is proportional to <br>
 *  intercept + slope * value per set
 *
 */
public class ValuePerSetFadModule
        extends DiscretizedOwnFadPlanningModule {

    //BP: I'm moving away from the 2 parameter model to a 1 parameter dampen model.
    //Dampening is a simpler interpretation
    //dampen=1: uniform at random across all regions
    //dampen=0: scale VPS to give probability
    private final double intercept;
    private final double slope;

    private final double dampen;


    public ValuePerSetFadModule(OwnFadSetDiscretizedActionGenerator optionsGenerator, double intercept, double slope) {
        super(optionsGenerator);
        this.intercept = intercept;
        this.slope = slope;
        this.dampen = -1;
        Preconditions.checkArgument(optionsGenerator.getMinimumFadValue()<=0,
                                    "by setting minimum value fad set >0 you bias the value per set computation!");
    }

    public ValuePerSetFadModule(OwnFadSetDiscretizedActionGenerator optionsGenerator, double dampen) {
        super(optionsGenerator);
        this.dampen = dampen;
        this.intercept = 0;
        this.slope=1;
        Preconditions.checkArgument(optionsGenerator.getMinimumFadValue()<=0,
                "by setting minimum value fad set >0 you bias the value per set computation!");
    }


    @Override
    protected PlannedAction chooseFadSet(
            Plan currentPlanSoFar, Fisher fisher, FishState model, NauticalMap map,
            OwnFadSetDiscretizedActionGenerator optionsGenerator) {
        List<uk.ac.ox.oxfish.utility.Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options =
                optionsGenerator.peekAllFads();

        //if there are no options, don't bother
        if(options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if(options.size()==1)
        {
            if(options.get(0).getSecond()>0)
                return optionsGenerator.chooseFad(options.get(0).getSecond());
            else return null;
        }

        //let's go through the value per set options
        double sumOfItAll = 0;
        List<Pair<Integer,Double>> probabilities = new LinkedList<>();
        for (uk.ac.ox.oxfish.utility.Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>,
                Integer> option : options) {

            double totalValueOfOption = 0;
            double numberOfOptions = 0;

            //sum up the raw $ amount you expect to make
            for (OwnFadSetDiscretizedActionGenerator.ValuedFad fadInGroup : option.getFirst()) {
                if(Double.isFinite(fadInGroup.getSecond()))
                    totalValueOfOption += fadInGroup.getSecond();
                numberOfOptions++;
            }

            double probability;
            if(dampen>-1){
                //The actual "probability" variation is dampened to be more like uniform at random
                probability = dampen*1 + (1-dampen)*totalValueOfOption / numberOfOptions;
            } else {
                //the actual "probability" is proportional to intercept + slope * SUM_OF_VALUE/NUMBER_OF_FADS
                probability = intercept + slope * totalValueOfOption / numberOfOptions;
            }
            probabilities.add(new Pair<>(option.getSecond(),
                                         probability)

            );
            sumOfItAll+=probability;

        }
        if(sumOfItAll<=0)
            return null;
        EnumeratedDistribution<Integer> sampler = new EnumeratedDistribution<>(
                new MTFApache(model.getRandom()),
                probabilities
        );


        Integer fadGroupChosen = sampler.sample();
        //all fads are empty, don't bother setting on any!
        if(fadGroupChosen<0 ||
                fadGroupChosen >= optionsGenerator.getNumberOfGroups() )
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);

    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }

    public double getDampen(){
        return dampen;
    }
}
