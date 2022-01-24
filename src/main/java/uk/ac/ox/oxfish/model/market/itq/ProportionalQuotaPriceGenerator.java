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

package uk.ac.ox.oxfish.model.market.itq;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.HashMap;

/**
 * My first attempt at generating reservation prices in a multi-species world.
 * Basically the value of the quota is not just the value of the fish you are selling but
 * also that you can keep selling other species.
 * Right now lambda(i) =  Pr(needed)*(profit for fish(i) + ratio of catches(j,i) (profit for fish(j) - lambda(j))
 * Created by carrknight on 10/6/15.
 */
public class ProportionalQuotaPriceGenerator  implements PriceGenerator, Steppable
{


    /**
     * the order book for each specie
     */
    private final HashMap<Integer,ITQOrderBook> orderBooks;

    /**
     * the index of the specie we want to compute the lambda of
     */
    final private int specieIndex;

    /**
     * the fisher who is thinking about this
     */
    private Fisher fisher;

    /**
     *  the fish-state
     */
    private FishState state;

    /**
     * the function that returns how many quotas are left!
     */
    private final Sensor<Fisher,Double> numberOfQuotasLeftGetter;


    /**
     * the lambda as it was last computed
     */
    private double lastLambda;

    /**
     * the stoppable to use when turning off
     */
    private Stoppable receipt;



    public ProportionalQuotaPriceGenerator(
            HashMap<Integer,ITQOrderBook> orderBooks, int specieIndex,
            Sensor<Fisher,Double> numberOfQuotasLeftGetter) {
        this.orderBooks = orderBooks;
        this.specieIndex = specieIndex;
        this.numberOfQuotasLeftGetter = numberOfQuotasLeftGetter;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        this.fisher = fisher;
        this.state = model;

        receipt = model.scheduleEveryDay(this, StepOrder.AGGREGATE_DATA_GATHERING);


        if(fisher.getDailyData().getColumn("Reservation Quota Price of " + model.getSpecies().get(specieIndex))==null) {
            fisher.getDailyData().registerGatherer("Reservation Quota Price of " + model.getSpecies().get(specieIndex),
                                                   fisher1 -> lastLambda,
                                                   Double.NaN);
        }
        else {
            assert model.getPorts().size() > 1;
        }
    }

    @Override
    public void step(SimState simState) {
        lastLambda = computeLambda();
    }

    public double computeLambda() {

        if(fisher == null)
            return Double.NaN;
        if (state.getDayOfTheYear() == 365)
            return Double.NaN;
        if( !fisher.isAllowedAtSea())
            return 0d;

        //if you have infinite quotas (unprotected species), you have no value for them
        Double quotasLeft = numberOfQuotasLeftGetter.scan(fisher);
        if(Double.isInfinite(quotasLeft))
            return Double.NaN;

        //if you expect to catch nothing, then the quota is worthless
        double dailyCatchesPredicted = fisher.predictDailyCatches(specieIndex);
        if(dailyCatchesPredicted < FishStateUtilities.EPSILON) //if you predict no catches a day, you don't value the quota at all (the probability will be 0)
        {
            assert dailyCatchesPredicted > -FishStateUtilities.EPSILON;
            return 0d;
        }

        //if you are not ready, you are not ready!
        if(Double.isNaN(dailyCatchesPredicted))
            return Double.NaN;

        assert dailyCatchesPredicted > 0 : dailyCatchesPredicted;

        //365 - state.getDayOfTheYear();
        int amountOfDaysLeftFishing = fisher.getDepartingStrategy().predictedDaysLeftFishingThisYear(fisher,state,state.getRandom());
        double probability = quotasLeft < FishStateUtilities.EPSILON ? 1 : 1 -
                fisher.probabilitySumDailyCatchesBelow(specieIndex, quotasLeft,
                        amountOfDaysLeftFishing);


        if(probability < FishStateUtilities.EPSILON) //if the probability is very low, skip computations, you value it nothing
            return 0d;

        probability = FishStateUtilities.round5(probability);

        double multiplier = fisher.predictUnitProfit(specieIndex);
        if(Double.isNaN(multiplier))
            //you  can't predict profits yet (predictor not ready, probably)
            return Double.NaN;


        //if you don't discard, you need to connect how much of other species' landings you make while fishing this one in particular
        //todo this is ugly
        if(fisher.getDiscardingStrategy() instanceof NoDiscarding) {
            //for every species
            for (int species = 0; species < state.getSpecies().size(); species++) {
                if (species == specieIndex) //don't count yourself
                    continue;
                //if we can't predict profits for this species (maybe because we never catch it) then don't count it
                double predictedCatches = fisher.predictDailyCatches(species);
                double predictedUnitProfit = fisher.predictUnitProfit(species);
                if (Double.isNaN(predictedCatches) ||
                        predictedCatches < FishStateUtilities.EPSILON ||
                        Double.isNaN(predictedUnitProfit))
                    continue;

                predictedCatches = FishStateUtilities.round5(predictedCatches);
                predictedUnitProfit = FishStateUtilities.round5(predictedUnitProfit);
                //quota price (0 if there is no market)
                ITQOrderBook market = orderBooks.get(species);
                double quotaPrice = market != null ? market.getLastClosingPrice() : 0;
                quotaPrice = Double.isFinite(quotaPrice) ? quotaPrice : 0; //value it 0 if it's NAN

                multiplier += (predictedUnitProfit - quotaPrice)
                        * predictedCatches / dailyCatchesPredicted;

                Preconditions.checkArgument(Double.isFinite(multiplier));
            }
        }
        return multiplier * probability;





    }


    @Override
    public void turnOff(Fisher fisher) {
        if(state!=null) {
            this.fisher.getDailyData().removeGatherer("Reservation Quota Price of " + state.getSpecies().get(specieIndex));
            receipt.stop();
        }
        this.fisher = null;
        this.state = null;
    }


    @Override
    public Fisher getFisher() {
        return fisher;
    }
}
