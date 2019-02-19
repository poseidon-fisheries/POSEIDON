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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * All the factories that build gears
 * Created by carrknight on 9/30/15.
 */
public class Gears {


    private Gears()
    {
    }


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends Gear>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{

        CONSTRUCTORS.put("Fixed Proportion",
                         FixedProportionGearFactory::new);
        NAMES.put(FixedProportionGearFactory.class,"Fixed Proportion");

        CONSTRUCTORS.put("One Species Gear",
                         OneSpecieGearFactory::new);
        NAMES.put(OneSpecieGearFactory.class,"One Species Gear");


        CONSTRUCTORS.put("Random Catchability",
                         RandomCatchabilityTrawlFactory::new);
        NAMES.put(RandomCatchabilityTrawlFactory.class,"Random Catchability");

        CONSTRUCTORS.put("Random Catchability By List",
                         RandomTrawlStringFactory::new);
        NAMES.put(RandomTrawlStringFactory.class, "Random Catchability By List");

        CONSTRUCTORS.put("Habitat Aware Gear",
                         HabitatAwareGearFactory::new);
        NAMES.put(HabitatAwareGearFactory.class,"Habitat Aware Gear");

        CONSTRUCTORS.put("Threshold Gear Factory",
                         ThresholdGearFactory::new);
        NAMES.put(ThresholdGearFactory.class,"Threshold Gear Factory");

        CONSTRUCTORS.put("Logistic Selectivity Gear",
                         LogisticSelectivityGearFactory::new);
        NAMES.put(LogisticSelectivityGearFactory.class,"Logistic Selectivity Gear");

        CONSTRUCTORS.put("Double Normal Selectivity Gear",
                         DoubleNormalGearFactory::new);
        NAMES.put(DoubleNormalGearFactory.class,"Double Normal Selectivity Gear");

        CONSTRUCTORS.put("Sablefish Trawl Selectivity Gear",
                         SablefishGearFactory::new);
        NAMES.put(SablefishGearFactory.class,"Sablefish Trawl Selectivity Gear");

        CONSTRUCTORS.put("Heterogeneous Selectivity Gear",
                         HeterogeneousGearFactory::new);
        NAMES.put(HeterogeneousGearFactory.class,"Heterogeneous Selectivity Gear");

        CONSTRUCTORS.put("Abundance Fixed Proportion Gear",
                         FixedProportionHomogeneousGearFactory::new);
        NAMES.put(FixedProportionHomogeneousGearFactory.class,"Abundance Fixed Proportion Gear");

        CONSTRUCTORS.put("Garbage Gear",
                         GarbageGearFactory::new);
        NAMES.put(GarbageGearFactory.class,"Garbage Gear");

        CONSTRUCTORS.put("Hold Upper Limit",
                         HoldLimitingDecoratorFactory::new);
        NAMES.put(HoldLimitingDecoratorFactory.class,"Hold Upper Limit");

        CONSTRUCTORS.put("Purse Seine Gear", PurseSeineGearFactory::new);
        NAMES.put(PurseSeineGearFactory.class, "Purse Seine Gear");

    }

}
