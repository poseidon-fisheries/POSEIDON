package uk.ac.ox.oxfish.utility.bandit.factory;

import sim.app.balls3d.Band;
import uk.ac.ox.oxfish.fisher.strategies.departing.AdaptiveProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.DoubleLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.MonthlyDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.UnifiedAmateurishDynamicFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.EpsilonGreedyBanditAlgorithm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by carrknight on 11/11/16.
 */
public class BanditAlgorithms {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<
            AlgorithmFactory<BanditSupplier>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();


    static{
        CONSTRUCTORS.put("Epsilon Greedy Bandit",
                         EpsilonGreedyBanditFactory::new);
        NAMES.put(EpsilonGreedyBanditFactory.class,"Epsilon Greedy Bandit");

        CONSTRUCTORS.put("Softmax Bandit",
                         SoftmaxBanditFactory::new);
        NAMES.put(SoftmaxBanditFactory.class,"Softmax Bandit");


        CONSTRUCTORS.put("UCB1 Bandit",
                         UCB1BanditFactory::new);
        NAMES.put(UCB1BanditFactory.class,"UCB1 Bandit");



    }


}
