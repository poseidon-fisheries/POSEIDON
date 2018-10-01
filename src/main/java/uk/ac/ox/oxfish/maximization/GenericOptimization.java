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
import uk.ac.ox.oxfish.maximization.generic.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GenericOptimization extends SimpleProblemDouble {

    public static final double MINIMUM_CATCHABILITY = 0.000001;

    public static final double MAXIMUM_CATCHABILITY = 0.0005;

    private static final Path DEFAULT_PATH = Paths.get("docs",
            "indonesia_hub",
            "runs", "712", "slice1", "calibration");

    private String scenarioFile =   DEFAULT_PATH.resolve("very_pessimistic.yaml").toString();


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
            parameters.add(new SimpleOptimizationParameter(
                    "fisherDefinitions$"+populations+".gear.delegate.proportionSimulatedToGarbage",
                    .10,
                    .8
            ));
        }
    }

    /**
     * map linking the name of the YearlyDataSet in the model with the path to file containing the real time series
     */
    private List<DataTarget> targets = new LinkedList<>();
    {

        Map<String,Integer> populations = new HashMap<>();
        populations.put("Small",0);
        populations.put("Medium",1);
        populations.put("Big",2);

        for (Map.Entry<String, Integer> population : populations.entrySet()) {
           targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LL021 Lutjanus malabaricus.csv").toString(),
                            "Lutjanus malabaricus Landings of population"+population.getValue(),true,-1d,1,
                            true));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LP012 Pristipomoides multidens.csv").toString(),
                            "Pristipomoides multidens Landings of population"+population.getValue(),true,-1d,1,
                            true));


                       targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_SE002 Epinephelus areolatus.csv").toString(),
                            "Epinephelus areolatus Landings of population"+population.getValue(),true,-1d,1,
                            true));
            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LL017 Lutjanus erythropterus.csv").toString(),
                            "Lutjanus erythropterus Landings of population"+population.getValue(),true,-1d,1,
                            true));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_other.csv").toString(),
                            "Others Landings of population"+population.getValue(),true,-1d,1,
                            true));

            targets.add(
                    new YearlyDataTarget(
                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_total.csv").toString(),
                            "Total Landings of population"+population.getValue(),true,-1d,1,
                            true));

            //biomass!
//            targets.add(
//                    new FixedDataLastStepTarget(
//                            17264818d,
//                            "Biomass Lutjanus malabaricus"
//                    ));
//            targets.add(
//                    new FixedDataLastStepTarget(
//                            9116571d,
//                            "Biomass Pristipomoides multidens"
//                    ));
//            targets.add(
//                    new FixedDataLastStepTarget(
//                            2738917d,
//                            "Biomass Epinephelus areolatus"
//                    ));
//            targets.add(
//                    new FixedDataLastStepTarget(
//                            1723360d,
//                            "Biomass Lutjanus erythropterus"
//                    ));
//
//
//            targets.add(
//                    FixedDataLastStepTarget.lastStepTarget(
//                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LL021 Lutjanus malabaricus.csv"),
//                            "Lutjanus malabaricus Landings of population"+population.getValue()));
//
//            targets.add(
//                    FixedDataLastStepTarget.lastStepTarget(
//                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LP012 Pristipomoides multidens.csv"),
//                            "Pristipomoides multidens Landings of population"+population.getValue()
//                    ));
//
//
//            targets.add(
//                    FixedDataLastStepTarget.lastStepTarget(
//                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_SE002 Epinephelus areolatus.csv"),
//                            "Epinephelus areolatus Landings of population"+population.getValue()
//                    ));
//            targets.add(
//                    FixedDataLastStepTarget.lastStepTarget(
//                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_LL017 Lutjanus erythropterus.csv"),
//                            "Lutjanus erythropterus Landings of population"+population.getValue()
//                    ));
//
//            targets.add(
//                    FixedDataLastStepTarget.lastStepTarget(
//                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_other.csv"),
//                            "Others Landings of population"+population.getValue()
//                    ));
//
//            targets.add(
//                    FixedDataLastStepTarget.lastStepTarget(
//                            DEFAULT_PATH.resolve("targets").resolve(population.getKey()+"_total.csv"),
//                            "Total Landings of population"+population.getValue()
//                    ));
        }




    }










    private int runsPerSetting = 1;

    private int simulatedYears = 4;

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

        try {
            double error = 0;

            for (int i = 0; i < runsPerSetting; i++) {
                //read in and modify parameters
                Scenario scenario = buildScenario(x);

                //run the model
                FishState model = new FishState(System.currentTimeMillis());
                model.setScenario(scenario);
                model.start();
                System.out.println("starting run");
                while (model.getYear() < simulatedYears) {
                    model.schedule.step(model);
                }
                model.schedule.step(model);

                //collect error
                for (DataTarget target : targets) {
                    error+=target.computeError(model);
                }

            }

            double finalError = error / (double) runsPerSetting;
            System.out.println(Arrays.toString(x) + " ---> " + finalError);
            return new double[]{finalError};

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Scenario buildScenario(double[] x) throws FileNotFoundException {
        FishYAML yaml = new FishYAML();
        Scenario scenario = yaml.loadAs(new FileReader(Paths.get(scenarioFile).toFile()),Scenario.class);
        int parameter=0;
        for (OptimizationParameter optimizationParameter : parameters)
        {
            optimizationParameter.parametrize(scenario,
                    Arrays.copyOfRange(x,parameter,
                            parameter+optimizationParameter.size()));
            parameter+=optimizationParameter.size();
        }
        return scenario;
    }


    public static void main(String[] args) throws IOException {
        GenericOptimization optimization = new GenericOptimization();
        Scenario scenario = optimization.buildScenario(new double[]{-4.079,-7.689,-8.910,-6.419,-1.280,-4.068, 8.367,-1.134, 2.389, 2.523, 1.465, 9.616,-2.031,-8.770,-4.383});
        FishYAML yaml = new FishYAML();
        yaml.dump(scenario,new FileWriter(DEFAULT_PATH.resolve("results").resolve("ga_14000_rbio_verypessimistic.yaml").toFile()));

    }


    public List<OptimizationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OptimizationParameter> parameters) {
        this.parameters = parameters;
    }

    public List<DataTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<DataTarget> targets) {
        this.targets = targets;
    }

    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }
}
