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
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * All the factories that build gears
 * Created by carrknight on 9/30/15.
 */
public class Gears {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends Gear>>> CONSTRUCTORS;

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(FixedProportionGearFactory.class, "Fixed Proportion");
        NAMES.put(OneSpecieGearFactory.class, "One Species Gear");
        NAMES.put(RandomCatchabilityTrawlFactory.class, "Random Catchability");
        NAMES.put(RandomTrawlStringFactory.class, "Random Catchability By List");
        NAMES.put(HabitatAwareGearFactory.class, "Habitat Aware Gear");
        NAMES.put(ThresholdGearFactory.class, "Threshold Gear Factory");
        NAMES.put(LogisticSelectivityGearFactory.class, "Logistic Selectivity Gear");
        NAMES.put(SimpleLogisticGearFactory.class, "Simple Logistic Selectivity Gear");
        NAMES.put(SelectivityFromListGearFactory.class, "Selectivity from List Gear");
        NAMES.put(SimpleDomeShapedGearFactory.class, "Simple Dome Shaped Selectivity Gear");
        NAMES.put(DoubleNormalGearFactory.class, "Double Normal Selectivity Gear");
        NAMES.put(SablefishGearFactory.class, "Sablefish Trawl Selectivity Gear");
        NAMES.put(HeterogeneousGearFactory.class, "Heterogeneous Selectivity Gear");
        NAMES.put(FixedProportionHomogeneousGearFactory.class, "Abundance Fixed Proportion Gear");
        NAMES.put(GarbageGearFactory.class, "Garbage Gear");
        NAMES.put(HoldLimitingDecoratorFactory.class, "Hold Upper Limit");
        NAMES.put(DelayGearDecoratorFactory.class, "Hour Delay Gear");
        NAMES.put(MaxThroughputDecoratorFactory.class, "Max Throughput Limit");
        NAMES.put(PurseSeineGearFactory.class, "Purse Seine Gear");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private Gears() { }
}
