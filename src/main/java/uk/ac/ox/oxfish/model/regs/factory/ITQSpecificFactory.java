package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.SpecificQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Like Mono factory but this quotas are not valid for all species but only for one of them
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
            ITQMarketBuilder initializer = new ITQMarketBuilder();
            //make sure it will start with the model
            state.registerStartable(initializer);
            //put it in the map so we only create it once
            marketBuilders.put(state, initializer);
        }
        ITQMarketBuilder marketBuilder = marketBuilders.get(state);
        assert marketBuilder != null;
        return new SpecificQuotaRegulation(individualQuota.apply(state.getRandom()),state,
                                           state.getSpecies().get(specieIndex))
        {

            /**
             * in addition tell the fisher to count opportunity costs
             */
            @Override
            public void reactToSale(Specie specie, Fisher seller, double biomass, double revenue) {
                //do the usual stuff
                super.reactToSale(specie, seller, biomass, revenue);

                //account for opportunity costs
                if(biomass > 0 && getProtectedSpecie().equals(specie))
                {
                    double lastClosingPrice = marketBuilder.getMarket().getLastClosingPrice();
                    if(Double.isFinite(lastClosingPrice))
                    {
                        seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
                    }
                }
            }
        };
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
