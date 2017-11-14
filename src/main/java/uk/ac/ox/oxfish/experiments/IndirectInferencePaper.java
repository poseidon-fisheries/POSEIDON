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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.LogitRPUEDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomThenBackToPortFactory;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class IndirectInferencePaper {


    /**
     * store list of names of the algorithms to use and their factory; this is used for the model-selection bit
     */
    private final static LinkedHashMap<String,
            AlgorithmFactory<? extends DestinationStrategy>> strategies =
            new LinkedHashMap<>();


    static {





        LogitRPUEDestinationFactory perfect = new LogitRPUEDestinationFactory();
        SquaresMapDiscretizerFactory discretizer = new SquaresMapDiscretizerFactory();
        discretizer.setHorizontalSplits(new FixedDoubleParameter(2));
        discretizer.setVerticalSplits(new FixedDoubleParameter(2));
        perfect.setDiscretizer(discretizer);
        strategies.put(
                "perfect3by3",
                perfect
        );


        //todo add 3 variants of explore-exploit-imitate
        //todo add a heatmapper
        //todo add a social annealing
        //todo add 3 variants of bandits


        strategies.put(
                "random",
                new RandomThenBackToPortFactory()
        );



    }






}
