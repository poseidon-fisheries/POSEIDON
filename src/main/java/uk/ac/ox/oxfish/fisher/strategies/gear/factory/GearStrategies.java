package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Created by carrknight on 6/14/16.
 */
public class GearStrategies {

    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends GearStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("Never Change Gear", FixedGearStrategyFactory::new);
        NAMES.put(FixedGearStrategyFactory.class,"Never Change Gear");


        CONSTRUCTORS.put("Periodic Gear Update from List", PeriodicUpdateFromListFactory::new);
        NAMES.put(PeriodicUpdateFromListFactory.class,"Periodic Gear Update from List");



        CONSTRUCTORS.put("Periodic Gear Update Mileage", PeriodicUpdateMileageFactory::new);
        NAMES.put(PeriodicUpdateMileageFactory.class,"Periodic Gear Update Mileage");



        CONSTRUCTORS.put("Periodic Gear Update Catchability", PeriodicUpdateCatchabilityFactory::new);
        NAMES.put(PeriodicUpdateCatchabilityFactory.class,"Periodic Gear Update Catchability");





    }

}
