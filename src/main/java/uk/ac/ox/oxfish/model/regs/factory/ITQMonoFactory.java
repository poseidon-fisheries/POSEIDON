package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates both individual quotas like the IQMonoFactory and a quota market for fishers to trade in
 * Created by carrknight on 8/26/15.
 */
public class ITQMonoFactory implements AlgorithmFactory<MonoQuotaRegulation>
{

    /**
     * one market only for each fish-state
     */
    private final Map<FishState,ITQMarketBuilder> marketBuilders = new HashMap<>(1);

    /**
     * quota available to each guy
     */
    private DoubleParameter individualQuota = new FixedDoubleParameter(5000);

    public ITQMonoFactory(double individualQuota) {
        this.individualQuota = new FixedDoubleParameter(individualQuota);
    }

    public ITQMonoFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MonoQuotaRegulation apply(FishState state) {
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
        return new MonoQuotaRegulation(individualQuota.apply(state.getRandom()),state){

            /**
             * in addition tell the fisher to count opportunity costs
             */
            @Override
            public void reactToSale(Specie specie, Fisher seller, double biomass, double revenue) {
                //do the usual stuff
                super.reactToSale(specie, seller, biomass, revenue);

                //account for opportunity costs
                if(biomass > 0)
                {
                    double lastClosingPrice = marketBuilder.getMarket().getLastClosingPrice();
                    if(Double.isFinite(lastClosingPrice))
                    {
                        //you could have sold those quotas!
                        seller.recordOpportunityCosts(lastClosingPrice * biomass);
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
}
