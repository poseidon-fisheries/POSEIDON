package uk.ac.ox.oxfish.model.data.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.MonthlyDepartingFactory;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.model.data.IterativeAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 11/11/16.
 */
public class Averages {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends Averager>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static{


        CONSTRUCTORS.put("Average",
                         IterativeAverageFactory::new);
        NAMES.put(IterativeAverageFactory.class,"Average");

        CONSTRUCTORS.put("Moving Average",
                         MovingAverageFactory::new);
        NAMES.put(MovingAverageFactory.class,"Moving Average");

        CONSTRUCTORS.put("Exponential Moving Average",
                         ExponentialMovingAverageFactory::new);
        NAMES.put(ExponentialMovingAverageFactory.class,"Exponential Moving Average");




    }




}
