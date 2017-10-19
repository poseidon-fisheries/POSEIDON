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

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.QuotaPerSpecieRegulation;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A simple method to compute quota values for agents. Works in isolation, that is considers the value of a quota
 * by the unit profit of the specie caught and not by the bottleneck effect it might have as a choke specie for other
 * quotas
 * Created by carrknight on 8/20/15.
 */
public class MonoQuotaPriceGenerator implements PriceGenerator, Steppable
{



    private final int specieIndex;

    private Fisher fisher;

    private FishState state;

    private MonoQuotaRegulation quotas;


    /**
     * whether to include or not daily profits in the reservation price computation. This was something I attempted
     * on 20151006 but I decided to abandon as I don't think it makes as much economic sense as I thought it did
     */
    private final boolean includeDailyProfits;
    /**
     * the title we registered our lambda to
     */
    private String dataTitle;
    /**
     * the lambda as it was last computed
     */
    private double lastLambda;

    /**
     * the stoppable to use when turning off
     */
    private Stoppable receipt;

    public MonoQuotaPriceGenerator(int specieIndex,
                                   boolean includeDailyProfits) {
        this.specieIndex = specieIndex;
        this.includeDailyProfits = includeDailyProfits;
    }

    @Override
    public void start(FishState model, Fisher fisher)
    {
        this.fisher = fisher;
        this.state = model;
        //only works with the right kind of regulation!
        if(fisher.getRegulation() instanceof MonoQuotaRegulation)
            quotas = (MonoQuotaRegulation) fisher.getRegulation();
        else
            //todo make this better
        //ugly hack to deal with multiple regulations: just grab the first one that shows up!
            quotas = (MonoQuotaRegulation)
                    ((MultipleRegulations) fisher.getRegulation()).getRegulations().stream().filter(
                    new Predicate<Regulation>() {
                        @Override
                        public boolean test(Regulation regulation) {
                            return regulation instanceof QuotaPerSpecieRegulation;
                        }
                    }).collect(Collectors.toList()).get(0);


        receipt = model.scheduleEveryDay(this, StepOrder.AGGREGATE_DATA_GATHERING);

        dataTitle = "Reservation Quota Price of " + model.getSpecies().get(specieIndex);
        fisher.getDailyData().registerGatherer(dataTitle,
                                               fisher1 -> lastLambda,
                                               Double.NaN);



    }

    @Override
    public void turnOff(Fisher fisher) {
        //todo remove gatherer
        if(receipt!=null) {
            receipt.stop();
            this.fisher.getDailyData().removeGatherer(dataTitle);
        }
        this.fisher = null;
        this.state = null;
    }

    @Override
    public void step(SimState simState) {
        lastLambda = computeLambda();
    }

    public double computeLambda()
    {

        if(fisher == null)
            return Double.NaN;
        if (state.getDayOfTheYear() == 365)
            return Double.NaN;
        double probability = 1 - fisher.probabilitySumDailyCatchesBelow(specieIndex,quotas.getQuotaRemaining(specieIndex),
                                                                        365-state.getDayOfTheYear());

        if(!includeDailyProfits)
            return  (probability * fisher.predictUnitProfit(specieIndex));
        else
            return  (probability * (fisher.predictUnitProfit(specieIndex) +  (365 - state.getDayOfTheYear()) * fisher.predictDailyProfits() ));




    }

    /** {@inheritDoc} */
    @Override
    public Fisher getFisher() {
        return fisher;
    }
}
