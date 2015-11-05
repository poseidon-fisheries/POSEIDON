package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.SpecificQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

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
        return new SpecificQuotaRegulation(individualQuota.apply(state.getRandom()),state,
                                           state.getSpecies().get(specieIndex))
        {

            /**
             * in addition tell the fisher to count opportunity costs
             */
            @Override
            public void reactToSale(Species species, Fisher seller, double biomass, double revenue) {
                //do the usual stuff
                super.reactToSale(species, seller, biomass, revenue);

                computeOpportunityCosts(species, seller, biomass, revenue, this, marketBuilder.getMarket());
            }
        };
    }


    /**
     * this is visible to be overridden by tests. It's just the method used to assign opportunity costs at the end
     * of the trip
     */
    @VisibleForTesting
    public void computeOpportunityCosts(Species species, Fisher seller, double biomass, double revenue,
                                        SpecificQuotaRegulation regulation, ITQOrderBook market)
    {
        //account for opportunity costs
        if(biomass > 0 && regulation.getProtectedSpecies().equals(species))
        {
            double lastClosingPrice = market.getLastClosingPrice();
            if(Double.isFinite(lastClosingPrice))
            {
                seller.recordOpportunityCosts(lastClosingPrice * biomass); //you could have sold those quotas!
            }
        }
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
