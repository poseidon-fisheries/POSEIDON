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

package uk.ac.ox.oxfish.experiments.allocatorexperiment;

import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AllocatorSlice1 {

    private final static double[] CALIBRATION_BEST = new double[]{-7.059, 3.367};

//one year step
//    solution data	: {-7.749,-1.692}
//    solution fit	: [ 13588.040000000037 ]
//    Overall best statistical data:
//    FunctionCalls | currentBest | meanFit | solution
//1170 | 53428.97999999672 | 164967.8673333325 | {-7.749,-1.692}


    //two year step
//    lowerLimitStepSize=5.0E-7; mutationStepSize=0.2; tau1=0.15; }; mutationProbability=1.0; selectionProbability=[ 0.0 ]; }
//        solution data	: {-7.059, 3.367}
//        solution fit	: [ 2245928.3999999985 ]
//        Overall best statistical data:
//        FunctionCalls | currentBest | meanFit | solution
//        1440 | 2539726.1800000053 | 3249050.7353333333 | {-7.059, 3.367}


    private final static Path MAIN_DIRECTORY = Paths.get("docs","20191004 allocator","slice1");

    private final static Path OPTIMIZATION_YAML_PATH =
            MAIN_DIRECTORY.resolve("calibration.yaml");

    private final static String CALIBRATED_SCENARIO_NAME = "slice1_calibrated_spinup";

    private final static int shockYear = 1;
    private static final int RUNS_PER_POLICY = 10;


    public static void main(String[] args) throws IOException {
        buildScenario();


        AllocatorSlice0.maxHoldSizeExperiment("all",
                                              new String[]{"population0","population1"},
                                              CALIBRATED_SCENARIO_NAME,
                                              2000000,
                                              50000, MAIN_DIRECTORY, MAIN_DIRECTORY, "Snapper", RUNS_PER_POLICY
        );

        AllocatorSlice0.maxHoldSizeExperiment("small",
                                              new String[]{"population0"},
                                              CALIBRATED_SCENARIO_NAME,
                                              2000000,
                                              50000, MAIN_DIRECTORY, MAIN_DIRECTORY, "Snapper", RUNS_PER_POLICY
        );

        AllocatorSlice0.maxHoldSizeExperiment("large",
                                              new String[]{"population1"},
                                              CALIBRATED_SCENARIO_NAME,
                                              2000000,
                                              50000, MAIN_DIRECTORY, MAIN_DIRECTORY, "Snapper", RUNS_PER_POLICY
        );

    }

    private static void buildScenario() throws IOException {
        FishYAML yaml = new FishYAML();

        GenericOptimization optimization =
                yaml.loadAs(new FileReader(OPTIMIZATION_YAML_PATH.toFile()),
                            GenericOptimization.class);

        Scenario scenario = GenericOptimization.buildScenario(CALIBRATION_BEST, Paths.get(optimization.getScenarioFile()).toFile(), optimization.getParameters());
        Path outputFile = MAIN_DIRECTORY.resolve(CALIBRATED_SCENARIO_NAME+".yaml");
        yaml.dump(scenario,new FileWriter(outputFile.toFile()));
    }


}
