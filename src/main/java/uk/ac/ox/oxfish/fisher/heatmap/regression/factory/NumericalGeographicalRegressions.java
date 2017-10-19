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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/5/16.
 */
public class NumericalGeographicalRegressions {


    private NumericalGeographicalRegressions() {
    }

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<
            ? extends GeographicalRegression<Double>>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{


        CONSTRUCTORS.put("Nearest Neighbor",
                         NearestNeighborRegressionFactory::new);
        NAMES.put(NearestNeighborRegressionFactory.class,"Nearest Neighbor");


        CONSTRUCTORS.put("Complete Nearest Neighbor",
                         CompleteNearestNeighborRegressionFactory::new);
        NAMES.put(CompleteNearestNeighborRegressionFactory.class,"Complete Nearest Neighbor");


        CONSTRUCTORS.put("Nearest Neighbor Transduction",
                         NearestNeighborTransductionFactory::new);
        NAMES.put(NearestNeighborTransductionFactory.class,"Nearest Neighbor Transduction");


        CONSTRUCTORS.put("Kernel Transduction",
                         KernelTransductionFactory::new);
        NAMES.put(KernelTransductionFactory.class,
                  "Kernel Transduction");


        CONSTRUCTORS.put("RBF Kernel Transduction",
                         DefaultRBFKernelTransductionFactory::new);
        NAMES.put(DefaultRBFKernelTransductionFactory.class,
                  "RBF Kernel Transduction");


        CONSTRUCTORS.put("Particle Filter Regression",
                         ParticleFilterRegressionFactory::new);
        NAMES.put(ParticleFilterRegressionFactory.class,
                  "Particle Filter Regression");


        CONSTRUCTORS.put("Simple Kalman",
                         SimpleKalmanRegressionFactory::new);
        NAMES.put(SimpleKalmanRegressionFactory.class,
                  "Simple Kalman");


        CONSTRUCTORS.put("GWR",
                         GeographicallyWeightedRegressionFactory::new);
        NAMES.put(GeographicallyWeightedRegressionFactory.class,
                  "GWR");


        CONSTRUCTORS.put("Good-Bad",
                         GoodBadRegressionFactory::new);
        NAMES.put(GoodBadRegressionFactory.class,
                  "Good-Bad");


        CONSTRUCTORS.put("Kernel Regression",
                         DefaultKernelRegressionFactory::new);
        NAMES.put(DefaultKernelRegressionFactory.class,
                  "Kernel Regression");


        CONSTRUCTORS.put("RBF Network",
                         RBFNetworkFactory::new);
        NAMES.put(RBFNetworkFactory.class,
                  "RBF Network");


        CONSTRUCTORS.put("Social Tuning",
                         SocialTuningRegressionFactory::new);
        NAMES.put(SocialTuningRegressionFactory.class,
                  "Social Tuning");


        CONSTRUCTORS.put("Personal Tuning",
                         PersonalTuningRegressionFactory::new);
        NAMES.put(PersonalTuningRegressionFactory.class,
                  "Personal Tuning");


    }
}
