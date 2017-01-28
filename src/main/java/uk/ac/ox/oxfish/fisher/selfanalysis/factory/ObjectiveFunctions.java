package uk.ac.ox.oxfish.fisher.selfanalysis.factory;


import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ObjectiveFunctions {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends ObjectiveFunction<Fisher>>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();




    static{
        CONSTRUCTORS.put("Cash Flow Objective",
                         CashFlowObjectiveFactory::new);
        NAMES.put(CashFlowObjectiveFactory.class,
                  "Cash Flow Objective");

        CONSTRUCTORS.put("Hourly Profit Objective",
                         HourlyProfitObjectiveFactory::new);
        NAMES.put(HourlyProfitObjectiveFactory.class,
                  "Hourly Profit Objective");


        CONSTRUCTORS.put("Target Species Hourly Profit",
                         TargetSpeciesObjectiveFactory::new);
        NAMES.put(TargetSpeciesObjectiveFactory.class,
                  "Target Species Hourly Profit");


        CONSTRUCTORS.put("Hourly Knife-Edge Objective",
                         KnifeEdgePerTripFactory::new);
        NAMES.put(KnifeEdgePerTripFactory.class,
                  "Hourly Knife-Edge Objective");

        CONSTRUCTORS.put("Hourly Cutoff Objective",
                         CutoffPerTripObjectiveFactory::new);
        NAMES.put(CutoffPerTripObjectiveFactory.class,
                  "Hourly Cutoff Objective");

        CONSTRUCTORS.put("Cash Flow Knife-Edge Objective",
                         KnifeEdgeCashflowFactory::new);
        NAMES.put(KnifeEdgeCashflowFactory.class,
                  "Cash Flow Knife-Edge Objective");


    }

    private ObjectiveFunctions() {}
}