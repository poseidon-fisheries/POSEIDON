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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.strategies.departing.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.UnifiedAmateurishDynamicFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

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
    public static final Map<String,Supplier<AlgorithmFactory<? extends DepartingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Fixed Probability Departing",
                         FixedProbabilityDepartingFactory::new);
        NAMES.put(FixedProbabilityDepartingFactory.class,"Fixed Probability Departing");
        CONSTRUCTORS.put("Adaptive Probability Departing",
                         AdaptiveProbabilityDepartingFactory::new);
        NAMES.put(AdaptiveProbabilityDepartingFactory.class,"Adaptive Probability Departing");
        CONSTRUCTORS.put("Fixed Rest",
                         FixedRestTimeDepartingFactory::new);
        NAMES.put(FixedRestTimeDepartingFactory.class,"Fixed Rest");
        CONSTRUCTORS.put("Double Logistic",
                         DoubleLogisticDepartingFactory::new);
        NAMES.put(DoubleLogisticDepartingFactory.class,"Double Logistic");

        CONSTRUCTORS.put("Monthly Departing",
                         MonthlyDepartingFactory::new);
        NAMES.put(MonthlyDepartingFactory.class,"Monthly Departing");

        CONSTRUCTORS.put("Unified Amateurish Dynamic Programming",
                         UnifiedAmateurishDynamicFactory::getInstance);
        NAMES.put(UnifiedAmateurishDynamicFactory.class,
                  "Unified Amateurish Dynamic Programming");


        CONSTRUCTORS.put("Max Hours Per Year",
                         MaxHoursPerYearDepartingFactory::new);
        NAMES.put(MaxHoursPerYearDepartingFactory.class,
                  "Max Hours Per Year");



        CONSTRUCTORS.put("WFS Longline",
                         LonglineFloridaLogisticDepartingFactory::new);
        NAMES.put(LonglineFloridaLogisticDepartingFactory.class,"WFS Longline");

        CONSTRUCTORS.put("WFS Handline",
                         FloridaLogisticDepartingFactory::new);
        NAMES.put(FloridaLogisticDepartingFactory.class, "WFS Handline");


        CONSTRUCTORS.put("Exit Decorator",
                         ExitDecoratorFactory::new);
        NAMES.put(ExitDecoratorFactory.class,"Exit Decorator");
    }

    private DepartingStrategies() {}






}
