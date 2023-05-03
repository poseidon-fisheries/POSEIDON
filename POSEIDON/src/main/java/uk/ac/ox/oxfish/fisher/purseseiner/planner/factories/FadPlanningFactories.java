package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.DiscretizedOwnFadPlanningModule;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FadPlanningFactories {


    public static final Map<String, Supplier<AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule>>>
        CONSTRUCTORS;

    @SuppressWarnings("rawtypes")
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES =
        new LinkedHashMap<>();

    static {
        NAMES.put(DiscretizedOwnFadPlanningFactory.class, "Centroid FAD Planning");
        NAMES.put(GreedyInsertionFadPlanningFactory.class, "Greedy FAD Module");
        NAMES.put(MarginalValueFadPlanningModuleFactory.class, "MVT FAD Module");
        NAMES.put(ValuePerSetPlanningModuleFactory.class, "VPS FAD Module");
        NAMES.put(WhereFadsAreFadModuleFactory.class, "Where Fads Are FAD Module");
        NAMES.put(WhereMoneyIsPlanningFactory.class, "Where Money Is FAD Module");

        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private FadPlanningFactories() {
    }

}
