package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.MonoQuotaPriceGenerator;
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
    private final Map<FishState,Startable> marketBuilder = new HashMap<>(1);

    /**
     * quota available to each guy
     */
    DoubleParameter individualQuota = new FixedDoubleParameter(5000);


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
        if(!marketBuilder.containsKey(state))
        {
            //if not, create it!
            Startable initializer = new Startable() {
                @Override
                public void start(FishState model) {

                    //create the market
                    ITQOrderBook market = new ITQOrderBook(0);
                    market.start(model);
                    //gather market data
                    model.getDailyDataSet().registerGatherer("ITQ Trades", state1 -> market.getDailyMatches(),
                                                             Double.NaN);
                    model.getDailyDataSet().registerGatherer("ITQ Prices", state1 -> market.getDailyAveragePrice(),
                                                             Double.NaN);

                    //and give to each fisher a price-maker
                    for(Fisher fisher : model.getFishers())
                    {
                        MonoQuotaPriceGenerator lambdaer = new MonoQuotaPriceGenerator(0);
                        lambdaer.start(model, fisher);
                        market.registerTrader(fisher, lambdaer);
                    }

                }

                @Override
                public void turnOff() {

                }
            };
            //make sure it will start with the model
            state.registerStartable(initializer);
            //put it in the map so we only create it once
            marketBuilder.put(state, initializer);
        }
        assert marketBuilder.containsKey(state);
        return new MonoQuotaRegulation(individualQuota.apply(state.getRandom()),state);
    }

    public DoubleParameter getIndividualQuota() {
        return individualQuota;
    }

    public void setIndividualQuota(DoubleParameter individualQuota) {
        this.individualQuota = individualQuota;
    }
}
