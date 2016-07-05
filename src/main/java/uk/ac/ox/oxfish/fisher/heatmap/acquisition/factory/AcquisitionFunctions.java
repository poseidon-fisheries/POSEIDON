package uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory;

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/5/16.
 */
public class AcquisitionFunctions {


    private AcquisitionFunctions() {
    }

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends AcquisitionFunction>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{

        CONSTRUCTORS.put("Exhaustive Search",
                         ExhaustiveAcquisitionFunctionFactory::new);
        NAMES.put(ExhaustiveAcquisitionFunctionFactory.class,"Exhaustive Search");


        CONSTRUCTORS.put("Hill-Climber Acquisition",
                         HillClimberAcquisitionFunctionFactory::new);
        NAMES.put(HillClimberAcquisitionFunctionFactory.class,"Hill-Climber Acquisition");





    }

}
