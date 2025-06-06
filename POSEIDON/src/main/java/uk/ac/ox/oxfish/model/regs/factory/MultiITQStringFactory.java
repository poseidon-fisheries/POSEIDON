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
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.MultiQuotaITQRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.YamlConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A more flexible, if slightly convoluted, way to instantiate an ITQ targeting only a few species
 * Created by carrknight on 11/9/15.
 */
public class MultiITQStringFactory implements AlgorithmFactory<MultiQuotaITQRegulation> {

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
     * The string we are going to turn into rule, "0:100 ,2:uniform 1 100" means that EACH FISHER gets 100 quotas a year
     * for species 0 and a random quota of 1 to 100 for species 2. The other species (like species 1)
     * are then assumed NOT TO BE PROTECTED by the quota (and can be fished out freely)
     */
    private String yearlyQuotaMaps = "0:5000";

    /**
     * can traders buy/sell multiple times in a day
     */
    private boolean allowMultipleTrades = false;

    /**
     * the size of quota units (kg) traded each match;
     * This can be either a simple number (at which point all quotas are traded at the same tick volume) or a map like
     * "0:100,2:50" at which point the volume traded per match is different for each species
     */
    private String minimumQuotaTraded = "100";

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaITQRegulation apply(final FishState state) {

        final int numberOfSpecies = state.getSpecies().size();
        //create map of quotas
        @SuppressWarnings("UnstableApiUsage") final Map<String, String> quotasInputted =
            Splitter.on(",")
                .withKeyValueSeparator(":")
                .split(yearlyQuotaMaps.trim());
        Preconditions.checkArgument(quotasInputted.size() > 0, "You provided no quota for the ITQ!");

        //here we store the quotas
        final double[] quotas = new double[numberOfSpecies];
        Arrays.fill(quotas, Double.POSITIVE_INFINITY);
        //read them in
        for (final Map.Entry<String, String> input : quotasInputted.entrySet()) {
            final double yearlyQuota = YamlConstructor
                .parseDoubleParameter(input.getValue().trim())
                .applyAsDouble(state.getRandom());
            Preconditions.checkArgument(yearlyQuota > 0);
            quotas[Integer.parseInt(input.getKey().trim())] = yearlyQuota;
        }


        //create function of tick sizes
        final Function<Integer, Integer> volumePerMatch;
        if (minimumQuotaTraded.contains(":")) {
            @SuppressWarnings("UnstableApiUsage") final Map<String, String> volumesIn =
                Splitter.on(",")
                    .withKeyValueSeparator(":")
                    .split(minimumQuotaTraded.trim());
            Preconditions.checkArgument(
                volumesIn.size() == quotasInputted.size(),
                "Mismatch between number of markets and minimum quota traded provided"
            );
            Preconditions.checkArgument(
                volumesIn.keySet().equals(quotasInputted.keySet()),
                "Mismatch between keys, some markets do not have minimimum quota trades or viceversa"
            );
            final int[] tradeTicks = new int[numberOfSpecies];
            for (final Map.Entry<String, String> input : volumesIn.entrySet()) {
                final int tradeTick = (int) Double.parseDouble(input.getValue().trim());
                Preconditions.checkArgument(tradeTick > 0);
                tradeTicks[Integer.parseInt(input.getKey().trim())] = tradeTick;
            }
            volumePerMatch = speciesIndex -> tradeTicks[speciesIndex];

        } else {
            //it's not a map, ergo all tick sizes are of the same size
            final int tradeTick = Integer.parseInt(minimumQuotaTraded.trim());

            volumePerMatch = speciesIndex -> tradeTick;
        }


        /*
         *      __  __   _   ___ _  _____ _____   ___ _   _ ___ _    ___  ___ ___  ___
         *     |  \/  | /_\ | _ \ |/ / __|_   _| | _ ) | | |_ _| |  |   \| __| _ \/ __|
         *     | |\/| |/ _ \|   / ' <| _|  | |   | _ \ |_| || || |__| |) | _||   /\__ \
         *     |_|  |_/_/ \_\_|_\_|\_\___| |_|   |___/\___/|___|____|___/|___|_|_\|___/
         *
         */

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


        MultiITQFactory.buildITQMarketsIfNeeded(state, numberOfSpecies, quotas, markets, builders,
            allowMultipleTrades, volumePerMatch
        );


        final MultiQuotaITQRegulation multiQuotaITQRegulation = new MultiQuotaITQRegulation(quotas, state,
            markets
        );
        for (final ITQMarketBuilder builder : builders)
            if (builder != null)
                builder.addTrader(multiQuotaITQRegulation);

        return multiQuotaITQRegulation;


    }

    public String getYearlyQuotaMaps() {
        return yearlyQuotaMaps;
    }

    public void setYearlyQuotaMaps(final String yearlyQuotaMaps) {
        this.yearlyQuotaMaps = yearlyQuotaMaps;
    }

    /**
     * Getter for property 'allowMultipleTrades'.
     *
     * @return Value for property 'allowMultipleTrades'.
     */
    public boolean isAllowMultipleTrades() {
        return allowMultipleTrades;
    }

    /**
     * Setter for property 'allowMultipleTrades'.
     *
     * @param allowMultipleTrades Value to set for property 'allowMultipleTrades'.
     */
    public void setAllowMultipleTrades(final boolean allowMultipleTrades) {
        this.allowMultipleTrades = allowMultipleTrades;
    }


    /**
     * Getter for property 'minimumQuotaTraded'.
     *
     * @return Value for property 'minimumQuotaTraded'.
     */
    public String getMinimumQuotaTraded() {
        return minimumQuotaTraded;
    }

    /**
     * Setter for property 'minimumQuotaTraded'.
     *
     * @param minimumQuotaTraded Value to set for property 'minimumQuotaTraded'.
     */
    public void setMinimumQuotaTraded(final String minimumQuotaTraded) {
        this.minimumQuotaTraded = minimumQuotaTraded;
    }


    /**
     * Getter for property 'orderBooksBuilder'.
     *
     * @return Value for property 'orderBooksBuilder'.
     */
    @SuppressWarnings("deprecation")
    public uk.ac.ox.oxfish.utility.Locker<String, ITQMarketBuilder[]> getOrderBooksBuilder() {
        return orderBooksBuilder;
    }
}
