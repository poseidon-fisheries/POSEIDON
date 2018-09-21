/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.maximization.generic.CommaMapOptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.YearlyDataTarget;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenericOptimization extends SimpleProblemDouble {

    public static final double MINIMUM_CATCHABILITY = 0.00001;

    public static final double MAXIMUM_CATCHABILITY = 0.005;

    private static final Path DEFAULT_PATH = Paths.get("docs",
                                                       "indonesia_hub",
                                                       "runs", "712", "slice1", "calibration");

    //todo have a summary outputting a CSV: parameter1,parameter2,...,parameterN,target1,...,targetN for logging purposes and also maybe IITP

    /**
     * list of all parameters that can be changed
     */
    private List<OptimizationParameter> parameters = new LinkedList<>();

    {

        for(int populations=0; populations<3; populations++) {
            //gear
            //catchabilities
            parameters.add(new CommaMapOptimizationParameter(
                    4, "fisherDefinitions$"+populations+".gear.delegate.delegate.catchabilityMap",
                    MINIMUM_CATCHABILITY,
                    MAXIMUM_CATCHABILITY
            ));
            //garbage collectors
            parameters.add(new CommaMapOptimizationParameter(
                    4,
                    "fisherDefinitions$"+populations+".gear.delegate.proportionSimulatedToGarbage",
                    .10,
                    0.30
            ));
        }
    }

    /**
     * map linking the name of the YearlyDataSet in the model with the path to file containing the real time series
     */
    private List<YearlyDataTarget> targetRepresentations = new LinkedList<>();
    {

        Map<String,Integer> populations = new HashMap<>();
        populations.put("Small",0);
        populations.put("Medium",1);
        populations.put("Big",2);

        for (Map.Entry<String, Integer> population : populations.entrySet()) {
            targetRepresentations.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_SE002 Epinephelus areolatus.csv").toString(),
                            "Epinephelus areolatus Landings of population"+population.getValue(),true,.8
                    ));
            targetRepresentations.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LP012 Pristipomoides multidens.csv").toString(),
                            "Pristipomoides multidens Landings of population"+population.getValue(),true,.1
                    ));
            targetRepresentations.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LL021 Lutjanus malabaricus.csv").toString(),
                            "Lutjanus malabaricus Landings of population"+population.getValue(),true,.1
                    ));
            targetRepresentations.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LL017 Lutjanus erythropterus.csv").toString(),
                            "Lutjanus erythropterus Landings of population"+population.getValue(),true,.8
                    ));
            targetRepresentations.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_other.csv").toString(),
                            "Other Landings of population"+population.getValue(),true,.1
                    ));

        }




    }



    private String scenarioFile =   DEFAULT_PATH.resolve("optimistic.yaml").toString();




    /**
     * Return the problem dimension.
     *
     * @return the problem dimension
     */
    @Override
    public int getProblemDimension() {
        int sum = 0;
        for(OptimizationParameter parameter : parameters)
            sum+=parameter.size();
        return sum;
    }

    /**
     * Evaluate a double vector representing a possible problem solution as
     * part of an individual in the EvA framework. This makes up the
     * target function to be evaluated.
     *
     * @param x a double vector to be evaluated
     * @return the fitness vector assigned to x as to the target function
     */
    @Override
    public double[] evaluate(double[] x) {
        throw new RuntimeException("Not done");
    }
}
