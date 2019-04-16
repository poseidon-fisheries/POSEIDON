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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.QuotaLimitDecorator;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * A map of string to constructors, good for gui
 * Created by carrknight on 5/28/15.
 */
public class FishingStrategies {

    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends FishingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("Fish Once", FishOnceFactory::new);
        NAMES.put(FishOnceFactory.class,"Fish Once");
        CONSTRUCTORS.put("Tow Limit", TowLimitFactory::new);
        NAMES.put(TowLimitFactory.class,"Tow Limit");
        CONSTRUCTORS.put("Quota Bound", QuotaLimitDecoratorFactory::new);
        NAMES.put(QuotaLimitDecoratorFactory.class,"Quota Bound");
        CONSTRUCTORS.put("Fish Until Full", FishUntilFullFactory::new);
        NAMES.put(FishUntilFullFactory.class,"Fish Until Full");
        CONSTRUCTORS.put("Until Full With Day Limit", MaximumStepsFactory::new);
        NAMES.put(MaximumStepsFactory.class,"Until Full With Day Limit");
        CONSTRUCTORS.put("WFS Logit Return", FloridaLogitReturnFactory::new);
        NAMES.put(FloridaLogitReturnFactory.class,"WFS Logit Return");
        CONSTRUCTORS.put("Random FAD Fishing Strategy", RandomFadFishingStrategyFactory::new);
        NAMES.put(RandomFadFishingStrategyFactory.class, "Random FAD Fishing Strategy");
        CONSTRUCTORS.put("Follow Plan FAD Fishing Strategy", FollowPlanFadFishingStrategyFactory::new);
        NAMES.put(FollowPlanFadFishingStrategyFactory.class, "Follow Plan FAD Fishing Strategy");

    }

}
