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

package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.WeightLimitMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Collections holding all the possible factories for markets
 * Created by carrknight on 8/11/15.
 */
public class Markets {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String, Supplier<AlgorithmFactory<? extends Market>>> CONSTRUCTORS;

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(FixedPriceMarketFactory.class, "Fixed Price Market");
        NAMES.put(AbundanceAwareFixedPriceMarketFactory.class, "Abundance Aware Fixed Price Market");
        NAMES.put(ArrayFixedPriceMarket.class, "Fixed Price Market Array");
        NAMES.put(CongestedMarketFactory.class, "Congested Market");
        NAMES.put(MACongestedMarketFactory.class, "Moving Average Congested Market");
        NAMES.put(ThreePricesMarketFactory.class, "Three Prices Market");
        NAMES.put(NPricesMarketFactory.class, "Many Prices Market");
        NAMES.put(SpeciesMarketMappedFactory.class, "Multiple Three Prices Markets");
        NAMES.put(ThreePricesWithPremium.class, "Three Prices Market with premium");
        NAMES.put(WeightLimitMarketFactory.class, "Weight Limit Market");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

}
