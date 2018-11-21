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

package uk.ac.ox.oxfish.utility.bandit.factory;

import sim.app.balls3d.Band;
import uk.ac.ox.oxfish.fisher.strategies.departing.AdaptiveProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.DoubleLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
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
