package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.ProportionalQuotaPriceGenerator;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.QuotaPerSpecieRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates individual quotas and a quota market like ITQMonoFactory but this works for multiple species
 *
 * Created by carrknight on 10/7/15.
 */
public class ITQMultiFactory implements AlgorithmFactory<MultiQuotaRegulation>
{


    /**
     * an array of order books for each "model" run
     */
    private final Map<FishState,ITQOrderBook[]> orderBooks = new HashMap<>(1);

    /**
     * an array of order book makers for each model run
     */
    private final Map<FishState,ITQMarketBuilder[]> orderBooksBuilder = new HashMap<>(1);

    /**
     * The ITQ yearly quota to give the fisher to fish the first species
     */
    private DoubleParameter quotaFirstSpecie = new FixedDoubleParameter(5000);

    /**
     * The ITQ yearly quota to give the fisher for any species that isn't the first
     */
    private DoubleParameter quotaOtherSpecies = new FixedDoubleParameter(5000);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState state)
    {
        int numberOfSpecies = state.getSpecies().size();
        assert numberOfSpecies>0;
        double[] quotas = new double[numberOfSpecies];
        quotas[0] = quotaFirstSpecie.apply(state.getRandom());
        for(int i=1; i<numberOfSpecies; i++)
            quotas[i] = quotaOtherSpecies.apply(state.getRandom());


        //if you haven't created the markets, do so now
        if(!orderBooks.containsKey(state))
        {
            ITQOrderBook[] markets = new ITQOrderBook[numberOfSpecies];
            orderBooks.put(state, markets);
            assert !orderBooksBuilder.containsKey(state);
            ITQMarketBuilder[] builders = new ITQMarketBuilder[numberOfSpecies];
            for(int i=0; i<builders.length; i++) {
                final int specieIndex = i;
                //creates a market builder
                builders[i] = new ITQMarketBuilder(i,
                                                   //create proportional quota price generator
                                                   () -> new ProportionalQuotaPriceGenerator(markets,
                                                                                             specieIndex,
                                                                                             //reads the fisher regulation which we know
                                                                                             //what it is because we are supplying it now
                                                                                             fisher -> ((QuotaPerSpecieRegulation) fisher.getRegulation()).getQuotaRemaining(specieIndex)));
                state.registerStartable(builders[i]);
                //after the builder starts it will create a market, copy it in the array
                state.registerStartable(new Startable() {
                    @Override
                    public void start(FishState model) {
                        markets[specieIndex] = builders[specieIndex].getMarket();
                    }

                    @Override
                    public void turnOff() {

                    }
                });
            }

            orderBooksBuilder.put(state,builders);




        }

        final ITQOrderBook[] orderBooks = ITQMultiFactory.this.orderBooks.get(state);


        return  new MultiQuotaRegulation(quotas,state){
            //compute opportunity costs!

            @Override
            public void reactToSale(
                    Species species, Fisher seller, double biomass, double revenue) {
                super.reactToSale(species, seller, biomass, revenue);
                if(biomass>0)
                {
                    double lastClosingPrice = orderBooks[species.getIndex()].getLastClosingPrice();

                    if(Double.isFinite(lastClosingPrice))
                    {
                        //you could have sold those quotas!
                        seller.recordOpportunityCosts(lastClosingPrice * biomass);
                    }

                }
            }
        };
    }


    public DoubleParameter getQuotaFirstSpecie() {
        return quotaFirstSpecie;
    }

    public void setQuotaFirstSpecie(DoubleParameter quotaFirstSpecie) {
        this.quotaFirstSpecie = quotaFirstSpecie;
    }

    public DoubleParameter getQuotaOtherSpecies() {
        return quotaOtherSpecies;
    }

    public void setQuotaOtherSpecies(DoubleParameter quotaOtherSpecies) {
        this.quotaOtherSpecies = quotaOtherSpecies;
    }
}
