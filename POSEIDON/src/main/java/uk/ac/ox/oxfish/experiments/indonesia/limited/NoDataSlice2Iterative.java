/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.experiments.noisespike.AcceptableRangePredicate;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * single run, not batch based like the original.
 * To be used by EasyABC and EVA (hence the SimpleProblemDouble) extension
 */
public class NoDataSlice2Iterative extends SimpleProblemDouble {



    private static final List<OptimizationParameter> parameters =
            NoDataSlice2.parameters;

    public static final List<AcceptableRangePredicate> predicates =
            NoDataSlice2.predicates;
    private static final int MIN_ACCEPTABLE_YEAR = 10;
    private static final int MAX_YEARS_TO_RUN = NoDataSlice2.MAX_YEARS_TO_RUN;

    private static FlexibleScenario setupScenario(double[] randomValues) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        FlexibleScenario scenario = yaml.loadAs(
                new FileReader(NoDataSlice2.SCENARIO_FILE.toFile())
                , FlexibleScenario.class);

        return parametrizeScenario(scenario,randomValues).getFirst();


    }

    public static Pair<FlexibleScenario,String[]> parametrizeScenario(
            FlexibleScenario scenario,
            double[] randomValues) {

        Preconditions.checkState(parameters.size()==randomValues.length);
        String[] values = new String[randomValues.length];
        for (int i = 0; i < randomValues.length; i++) {


            values[i] =
                    parameters.get(i).parametrize(scenario,
                                                  new double[]{randomValues[i]});


        }


        return new Pair<>(scenario,values);
    }


    public static double runModelOnce(Scenario scenarioToRun,
                                                 int maxYearsToRun,
                                                 long seed,
                                                 int minAcceptableYear){

        //run the model
        FishState model = new FishState(seed);
        model.setScenario(scenarioToRun);
        model.start();
        while (model.getYear() <= maxYearsToRun) {
            model.schedule.step(model);
        }
        model.schedule.step(model);


        Integer validYear = null;
        double minDistance = Double.MAX_VALUE;
        for (validYear = maxYearsToRun; validYear > minAcceptableYear; validYear--) {

            double distance = 0;
            boolean valid = true;
            for (AcceptableRangePredicate predicate : predicates) {
                valid= valid & predicate.test(model,validYear);
                distance += predicate.distance(model,validYear);
            }
            System.out.println(validYear + " -- " + valid);

            if(Double.isFinite(distance) && distance < minDistance)
            {
                minDistance = distance;
            }



            if(valid)
                break;;


        }

        System.out.println("distance: " + minDistance);
        return minDistance;

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

        Preconditions.checkArgument(x.length == parameters.size());
        double result = Double.NaN;
        try {
            result = runModelOnce(
                    setupScenario(x),
                    MAX_YEARS_TO_RUN,
                    0,
                    MIN_ACCEPTABLE_YEAR
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Scenario file not found!!");
        }

        return new double[]{
                result

        };

    }

    /**
     * Return the problem dimension.
     *
     * @return the problem dimension
     */
    @Override
    public int getProblemDimension() {
        return parameters.size();
    }
}
