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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.ITQCostManager;
import uk.ac.ox.oxfish.model.regs.SpecificQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Like Mono factory but these quotas are not valid for all species but only for one of them
 * Created by carrknight on 9/22/15.
 */
public class ITQSpecificFactory implements AlgorithmFactory<SpecificQuotaRegulation>
{


    /**
     * one market only for each fish-state
     */
    private final Map<FishState,ITQMarketBuilder> marketBuilders = new HashMap<>(1);

    /**
     * quota available to each guy
     */
    private DoubleParameter individualQuota = new FixedDoubleParameter(5000);

    /**
     * the specie chosen
     */
    private int specieIndex = 0;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SpecificQuotaRegulation apply(FishState state) {
        //todo need to make this for multiple species

        //did we create a market already?
        if(!marketBuilders.containsKey(state))
        {
            //if not, create it!
            ITQMarketBuilder initializer = new ITQMarketBuilder(0);
            //make sure it will start with the model
            state.registerStartable(initializer);
            //put it in the map so we only create it once
            marketBuilders.put(state, initializer);
        }
        ITQMarketBuilder marketBuilder = marketBuilders.get(state);
        assert marketBuilder != null;
        final Species protectedSpecies = state.getSpecies().get(specieIndex);
        //now I need to add the opportunity cost
        ITQCostManager cost = new ITQCostManager(new Function<Species, ITQOrderBook>() {
            @Override
            public ITQOrderBook apply(Species species) {
                if(species== protectedSpecies)
                    return marketBuilder.getMarket();
                else
                    return null;
            }
        });
        SpecificQuotaRegulation regulation = new SpecificQuotaRegulation(
                individualQuota.apply(state.getRandom()), state,
                protectedSpecies)
        {
            @Override
            public void start(FishState model, Fisher fisher) {
                super.start(model, fisher);
                fisher.getOpportunityCosts().add(cost);
            }

            @Override
            public void turnOff(Fisher fisher) {
                super.turnOff(fisher);
                fisher.getOpportunityCosts().remove(cost);
            }
        };


        marketBuilder.addTrader(regulation);
        return regulation;
    }





    public DoubleParameter getIndividualQuota() {
        return individualQuota;
    }

    public void setIndividualQuota(DoubleParameter individualQuota) {
        this.individualQuota = individualQuota;
    }

    public int getSpecieIndex() {
        return specieIndex;
    }

    public void setSpecieIndex(int specieIndex) {
        this.specieIndex = specieIndex;
    }
}
