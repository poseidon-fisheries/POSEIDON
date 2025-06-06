/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.market.itq.ProportionalQuotaPriceGenerator;
import uk.ac.ox.oxfish.model.regs.MultiQuotaITQRegulation;
import uk.ac.ox.oxfish.model.regs.QuotaPerSpecieRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Creates individual quotas and a quota market like ITQMonoFactory but this works for multiple species
 * <p>
 * Created by carrknight on 10/7/15.
 */
public class MultiITQFactory implements AlgorithmFactory<MultiQuotaITQRegulation> {


    /**
     * an array of order books for each "model" lspiRun
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, HashMap<Integer, ITQOrderBook>> orderBooks =
        new uk.ac.ox.oxfish.utility.Locker<>();

    /**
     * an array of order book makers for each model lspiRun
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, ITQMarketBuilder[]> orderBooksBuilder =
        new uk.ac.ox.oxfish.utility.Locker<>();

    /**
     * The ITQ yearly quota to give the fisher to fish the first species
     */
    private DoubleParameter quotaFirstSpecie = new FixedDoubleParameter(5000);

    /**
     * The ITQ yearly quota to give the fisher for any species that isn't the first
     */
    private DoubleParameter quotaOtherSpecies = new FixedDoubleParameter(5000);


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
    public MultiQuotaITQRegulation apply(final FishState state) {
        final int numberOfSpecies = state.getSpecies().size();
        assert numberOfSpecies > 0;
        final double[] quotas = new double[numberOfSpecies];
        quotas[0] = quotaFirstSpecie.applyAsDouble(state.getRandom());
        for (int i = 1; i < numberOfSpecies; i++)
            quotas[i] = quotaOtherSpecies.applyAsDouble(state.getRandom());


        //grab the markets and its builders
        final HashMap<Integer, ITQOrderBook> markets =
            orderBooks.presentKey(
                state.getUniqueID(),
                HashMap::new
            );


        final ITQMarketBuilder[] builders = orderBooksBuilder.
            presentKey(
                state.getUniqueID(),
                () -> new ITQMarketBuilder[numberOfSpecies]
            );


        /*
         *      __  __   _   ___ _  _____ _____   ___ _   _ ___ _    ___  ___ ___  ___
         *     |  \/  | /_\ | _ \ |/ / __|_   _| | _ ) | | |_ _| |  |   \| __| _ \/ __|
         *     | |\/| |/ _ \|   / ' <| _|  | |   | _ \ |_| || || |__| |) | _||   /\__ \
         *     |_|  |_/_/ \_\_|_\_|\_\___| |_|   |___/\___/|___|____|___/|___|_|_\|___/
         *
         */
        buildITQMarketsIfNeeded(state, numberOfSpecies, quotas, markets, builders,
            allowMultipleTrades, integer -> minimumQuotaTraded
        );

        final MultiQuotaITQRegulation multiQuotaITQRegulation = new MultiQuotaITQRegulation(quotas, state,
            markets
        );

        for (final ITQMarketBuilder builder : builders)
            if (builder != null)
                builder.addTrader(multiQuotaITQRegulation);
        return multiQuotaITQRegulation;
    }

    /**
     * creates ITQ markets by instantiating and registering an ITQMarketBuilder for all species where the current fisher
     * has non-infinite yearly quotas. Avoids building a market if it is already registered
     *
     * @param state                        the model
     * @param numberOfSpecies              the number of species
     * @param quotas                       yealy quotas of this fisher
     * @param markets                      ITQ markets
     * @param builders                     ITQ builders
     * @param allowMultipleTradesPerFisher whether a fisher can make multiple trades within the same step
     * @param unitsTradedPerMatch          the size of quotas exchanged at each trade (in kg) as a function index of species ---> size of quota
     */
    public static void buildITQMarketsIfNeeded(
        final FishState state, final int numberOfSpecies, final double[] quotas,
        final HashMap<Integer, ITQOrderBook> markets,
        final ITQMarketBuilder[] builders, final boolean allowMultipleTradesPerFisher,
        final Function<Integer, Integer> unitsTradedPerMatch
    ) {

        Logger.getGlobal().fine(() -> "Building ITQ Markets for the following quotas: " + Arrays.toString(quotas));


        //for each species
        for (int i = 0; i < builders.length; i++) {
            final int specieIndex = i; //little trick for anonymous instantiation
            //if this particular market needs to be instantiated
            if (builders[i] == null) {
                //and at least this guy isn't given infinite quotas:
                final double quotaGiven = quotas[i];
                Preconditions.checkArgument(quotaGiven >= 0);
                Preconditions.checkArgument(!Double.isNaN(quotaGiven));
                if (Double.isFinite(quotaGiven)) {
                    //theeeeen build the market
                    builders[i] = new ITQMarketBuilder(
                        i,
                        //create proportional quota price generator
                        () -> new ProportionalQuotaPriceGenerator(
                            markets,
                            specieIndex,
                            //reads the fisher regulation which we know
                            //what it is because we are supplying it now
                            (Sensor<Fisher, Double>) fisher -> ((QuotaPerSpecieRegulation) fisher.getRegulation()).getQuotaRemaining(
                                specieIndex)
                        )
                    );
                    final int speciesIndex = i;
                    final Startable setupStep = new Startable() {
                        @Override
                        public void start(final FishState model) {
                            builders[speciesIndex].start(model);
                            final ITQOrderBook market = builders[specieIndex].getMarket();
                            markets.put(specieIndex, market);
                            market.setAllowMultipleTradesPerFisher(allowMultipleTradesPerFisher);
                            market.setUnitsTradedPerMatch(unitsTradedPerMatch.apply(specieIndex));
                        }

                        @Override
                        public void turnOff() {

                        }
                    };
                    //after the builder starts it will create a market, copy it in the array
                    if (!state.isStarted()) {

                        state.registerStartable(setupStep);
                    } else {
                        state.scheduleOnce((Steppable) simState -> setupStep.start(state), StepOrder.DAWN);
                    }
                }
            }
        }
    }

    public DoubleParameter getQuotaFirstSpecie() {
        return quotaFirstSpecie;
    }

    public void setQuotaFirstSpecie(final DoubleParameter quotaFirstSpecie) {
        this.quotaFirstSpecie = quotaFirstSpecie;
    }

    public DoubleParameter getQuotaOtherSpecies() {
        return quotaOtherSpecies;
    }

    public void setQuotaOtherSpecies(final DoubleParameter quotaOtherSpecies) {
        this.quotaOtherSpecies = quotaOtherSpecies;
    }

    public boolean isAllowMultipleTrades() {
        return allowMultipleTrades;
    }

    public void setAllowMultipleTrades(final boolean allowMultipleTrades) {
        this.allowMultipleTrades = allowMultipleTrades;
    }

    public int getMinimumQuotaTraded() {
        return minimumQuotaTraded;
    }

    public void setMinimumQuotaTraded(final int minimumQuotaTraded) {
        this.minimumQuotaTraded = minimumQuotaTraded;
    }
}
