package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.ITQCostManager;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Creates both individual quotas like the IQMonoFactory and a quota market for fishers to trade in
 * Created by carrknight on 8/26/15.
 */
public class ITQMonoFactory implements AlgorithmFactory<MonoQuotaRegulation>
{

    /**
     * one market only for each fish-state
     */
    private final Locker<FishState,ITQMarketBuilder> marketBuilders = new Locker<>();

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


        ITQMarketBuilder marketBuilder =
                marketBuilders.presentKey(state,
                                          new Supplier<ITQMarketBuilder>() {
                                              @Override
                                              public ITQMarketBuilder get() {
                                                  //if not, create it!
                                                  ITQMarketBuilder initializer = new ITQMarketBuilder(0);
                                                  //make sure it will start with the model
                                                  state.registerStartable(initializer);
                                                  return initializer;
                                              }
                                          });

        assert marketBuilder != null;

        ITQCostManager cost = new ITQCostManager(new Function<Species, ITQOrderBook>() {
            @Override
            public ITQOrderBook apply(Species species) {
                return marketBuilder.getMarket();
            }
        });

        MonoQuotaRegulation toReturn = new MonoQuotaRegulation(individualQuota.apply(state.getRandom())
        ) {

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
        marketBuilder.addTrader(toReturn);
        return toReturn;
    }

    public DoubleParameter getIndividualQuota() {
        return individualQuota;
    }

    public void setIndividualQuota(DoubleParameter individualQuota) {
        this.individualQuota = individualQuota;
    }
}
