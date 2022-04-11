/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.GenerateRandomPlansStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.UnifiedAmateurishDynamicFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Here I hold the strategy factory
 * Created by carrknight on 5/19/15.
 */
public class DepartingStrategies {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends DepartingStrategy>>> CONSTRUCTORS;

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(FixedProbabilityDepartingFactory.class, "Fixed Probability Departing");
        NAMES.put(AdaptiveProbabilityDepartingFactory.class, "Adaptive Probability Departing");
        NAMES.put(FixedRestTimeDepartingFactory.class, "Fixed Rest");
        NAMES.put(DoubleLogisticDepartingFactory.class, "Double Logistic");
        NAMES.put(MonthlyDepartingFactory.class, "Monthly Departing");
        NAMES.put(UnifiedAmateurishDynamicFactory.class, "Unified Amateurish Dynamic Programming");
        NAMES.put(MaxHoursPerYearDepartingFactory.class, "Max Hours Per Year");
        NAMES.put(MaxHoursOutWithRestingTimeDepartingStrategy.class, "Max Hours Per Year Plus Resting Time");
        NAMES.put(LonglineFloridaLogisticDepartingFactory.class, "WFS Longline");
        NAMES.put(FloridaLogisticDepartingFactory.class, "WFS Handline");
        NAMES.put(ExitDecoratorFactory.class, "Exit Decorator");
        NAMES.put(FullSeasonalRetiredDecoratorFactory.class, "Full-time Seasonal Retired Decorator");
        NAMES.put(PurseSeinerDepartingStrategyFactory.class, "Purse Seiner Departing Strategy");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private DepartingStrategies() {}

}
