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
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/5/16.
 */
public class NumericalGeographicalRegressions {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends GeographicalRegression<Double>>>> CONSTRUCTORS;

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(NearestNeighborRegressionFactory.class, "Nearest Neighbor");
        NAMES.put(CompleteNearestNeighborRegressionFactory.class, "Complete Nearest Neighbor");
        NAMES.put(NearestNeighborTransductionFactory.class, "Nearest Neighbor Transduction");
        NAMES.put(KernelTransductionFactory.class, "Kernel Transduction");
        NAMES.put(DefaultRBFKernelTransductionFactory.class, "RBF Kernel Transduction");
        NAMES.put(ParticleFilterRegressionFactory.class, "Particle Filter Regression");
        NAMES.put(SimpleKalmanRegressionFactory.class, "Simple Kalman");
        NAMES.put(GeographicallyWeightedRegressionFactory.class, "GWR");
        NAMES.put(GoodBadRegressionFactory.class, "Good-Bad");
        NAMES.put(DefaultKernelRegressionFactory.class, "Kernel Regression");
        NAMES.put(RBFNetworkFactory.class, "RBF Network");
        NAMES.put(SocialTuningRegressionFactory.class, "Social Tuning");
        NAMES.put(PersonalTuningRegressionFactory.class, "Personal Tuning");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private NumericalGeographicalRegressions() { }
}
