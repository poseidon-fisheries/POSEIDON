package uk.ac.ox.oxfish.model.regs.factory;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.ProportionalQuotaPriceGenerator;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.QuotaPerSpecieRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates individual quotas and a quota market like ITQMonoFactory but this works for multiple species
 *
 * Created by carrknight on 10/7/15.
 */
public class MultiITQFactory implements AlgorithmFactory<MultiQuotaRegulation>
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

    private Counter opportunityCostCounter;


    /**
     * can traders buy/sell multiple times in a day
     */
    private boolean allowMultipleTrades = false;

    /**
     * the size of quota units (kg) traded each day
     */
    private int minimumQuotaTraded = 100;

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



        /***
         *      __  __   _   ___ _  _____ _____   ___ _   _ ___ _    ___  ___ ___  ___
         *     |  \/  | /_\ | _ \ |/ / __|_   _| | _ ) | | |_ _| |  |   \| __| _ \/ __|
         *     | |\/| |/ _ \|   / ' <| _|  | |   | _ \ |_| || || |__| |) | _||   /\__ \
         *     |_|  |_/_/ \_\_|_\_|\_\___| |_|   |___/\___/|___|____|___/|___|_|_\|___/
         *
         */
        buildITQMarketsIfNeeded(state, numberOfSpecies, quotas, orderBooks, orderBooksBuilder,
                                allowMultipleTrades, minimumQuotaTraded);

        return MultiITQFactory.opportunityCostAwareQuotaRegulation(state,quotas,orderBooks.get(state));
    }

    /**
     * creates ITQ markets by instantiating and registering an ITQMarketBuilder for all species where the current fisher
     * has non-infinite yearly quotas. Avoids building a market if it is already registered
     * @param state the model
     * @param numberOfSpecies the number of species
     * @param quotas yealy quotas of this fisher
     * @param orderBooks a map model---> ITQ markets
     * @param orderBooksBuilder a map: model---> ITQ builders
     * @param allowMultipleTradesPerFisher whether a fisher can make multiple trades within the same step
     * @param unitsTradedPerMatch the size of quotas exchanged at each trade (in kg)
     */
    public static void buildITQMarketsIfNeeded(
            FishState state, int numberOfSpecies, double[] quotas, Map<FishState, ITQOrderBook[]> orderBooks,
            Map<FishState, ITQMarketBuilder[]> orderBooksBuilder, final boolean allowMultipleTradesPerFisher,
            final int unitsTradedPerMatch) {

        if(Log.TRACE)
            Log.trace("Building ITQ Markets for the following quotas: " + Arrays.toString(quotas));


        //perfect, if needed create a market container/market builder container
        if(!orderBooks.containsKey(state)) {
            orderBooks.put(state, new ITQOrderBook[numberOfSpecies]);
            orderBooksBuilder.put(state, new ITQMarketBuilder[numberOfSpecies]);
        }
        //grab the markets and its builders
        ITQOrderBook[] markets = orderBooks.get(state);
        ITQMarketBuilder[] builders = orderBooksBuilder.get(state);

        //for each species
        for(int i = 0; i < builders.length; i++)
        {
            final int specieIndex = i; //little trick for anonymous instantiation
            //if this particular market needs to be instantiated
            if(builders[i]== null)
            {
                //and at least this guy isn't given infinite quotas:
                double quotaGiven = quotas[i];
                Preconditions.checkArgument(quotaGiven >= 0);
                Preconditions.checkArgument(!Double.isNaN(quotaGiven));
                if (Double.isFinite(quotaGiven)) {
                    //theeeeen build the market
                    builders[i] = new ITQMarketBuilder(i,
                                                       //create proportional quota price generator
                                                       () -> new ProportionalQuotaPriceGenerator(markets,
                                                                                                 specieIndex,
                                                                                                 //reads the fisher regulation which we know
                                                                                                 //what it is because we are supplying it now
                                                                                                 fisher ->
                                                                                                         ((QuotaPerSpecieRegulation) fisher.getRegulation()).getQuotaRemaining(specieIndex)));
                    state.registerStartable(builders[i]);
                    //after the builder starts it will create a market, copy it in the array
                    state.registerStartable(new Startable() {
                        @Override
                        public void start(FishState model) {
                            markets[specieIndex] = builders[specieIndex].getMarket();
                            markets[specieIndex].setAllowMultipleTradesPerFisher(allowMultipleTradesPerFisher);
                            markets[specieIndex].setUnitsTradedPerMatch(unitsTradedPerMatch);
                        }

                        @Override
                        public void turnOff() {

                        }
                    });

                }
            }
        }
    }

    public static MultiQuotaRegulation opportunityCostAwareQuotaRegulation(
            final FishState state, final double[] quotas, final ITQOrderBook[] orderBooks)
    {
        return new MultiQuotaRegulation(quotas, state){
            //compute opportunity costs!



            @Override
            public void reactToSale(
                    Species species, Fisher seller, double biomass, double revenue) {
                super.reactToSale(species, seller, biomass, revenue);
                if(biomass>0 && orderBooks[species.getIndex()]!= null )
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

    public boolean isAllowMultipleTrades() {
        return allowMultipleTrades;
    }

    public void setAllowMultipleTrades(boolean allowMultipleTrades) {
        this.allowMultipleTrades = allowMultipleTrades;
    }

    public int getMinimumQuotaTraded() {
        return minimumQuotaTraded;
    }

    public void setMinimumQuotaTraded(int minimumQuotaTraded) {
        this.minimumQuotaTraded = minimumQuotaTraded;
    }
}
