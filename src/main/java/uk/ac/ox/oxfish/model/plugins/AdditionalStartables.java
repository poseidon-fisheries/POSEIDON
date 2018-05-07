package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.fisher.strategies.discarding.*;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class AdditionalStartables {

    private AdditionalStartables(){}

    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends AdditionalStartable>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();


    static {
        CONSTRUCTORS.put("Tow Heatmapper",
                TowAndAltitudePluginFactory::new
        );
        NAMES.put(TowAndAltitudePluginFactory.class,
                "Tow Heatmapper");



    }

}
