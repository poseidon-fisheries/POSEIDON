/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.fads;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.LinearClorophillAttractorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LinearEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityAttractorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

public class FadInitializerFactories {

    public static final Map<String, Supplier<AlgorithmFactory<? extends FadInitializerFactory>>>
        CONSTRUCTORS;

    @SuppressWarnings("rawtypes")
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES =
        new LinkedHashMap<>();

    static {
        NAMES.put(AbundanceFadInitializerFactory.class, "Abundance FAD Initializer");
        NAMES.put(LinearAbundanceFadInitializerFactory.class, "Linear Abundance FAD Initializer");
        NAMES.put(BiomassFadInitializerFactory.class, "Biomass FAD Initializer");

        NAMES.put(AbundanceFadInitializerBetaFactory.class, "Abundance FAD Beta Initializer");
        NAMES.put(AbundanceFadInitializerBetaFactoryWithExpiration.class, "Abundance FAD Beta Initializer With Expiration");
        //weibul linear intervals
        NAMES.put(WeibullLinearIntervalAttractorFactory.class, "Weibull FAD Linear Interval Initializer");
        NAMES.put(WeibullLinearIntervalEnvironmentalAttractorFactory.class, "Weibull FAD Linear Interval Environmental Initializer");
        //weibull linear catchability
        NAMES.put(WeibullCatchabilitySelectivityAttractorFactory.class, "Weibull FAD Catchability Selectivity Initializer");
        NAMES.put(WeibullCatchabilitySelectivityEnvironmentalAttractorFactory.class,
                  "Weibull FAD Catchability Selectivity Environmental Initializer");

        //linear catchabilities
        NAMES.put(AbundanceLinearIntervalInitializerFactory.class, "Abundance FAD Linear Interval Initializer");
        NAMES.put(LinearClorophillAttractorFactory.class,
                "Linear FAD Catchability Selectivity Clorophill Initializer");

        NAMES.put(LinearEnvironmentalAttractorFactory.class,
                  "Linear FAD Catchability Selectivity Environmental Initializer");


        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    public FadInitializerFactories() {
    }
}