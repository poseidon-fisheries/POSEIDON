/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.model.scenario.EpoFadsOnlyAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NoBoatsFadMatchingAnalysis {

    private final static double[] BEST_SOLUTION =
            //fad duds
            //new double[]{6.010, 9.067, 3.156,-3.473, 2.145,-10.000,-0.516, 2.314, 4.069,-8.192,-7.080,-7.547, 10.000,-5.177};
    //weibull
            new double[]{-8.396,-8.842,-5.499, 1.593, 6.768,-9.674,-0.747, 10.000,-3.828,-10.000, 9.576};

    private static final Path CALIBRATION_FILE =
            //fad duds:
//            Paths.get(
//            "docs","20220208 noboats_tuna","calibration","new_currents","carrknight",
//            "2022-03-02_15.01.42_withduds3mo","calibration_withfads.yaml"
//    );
    //weibull:
    Paths.get("docs/20220208 noboats_tuna/calibration/new_currents/carrknight/2022-03-07_20.56.45_weibull/calibration_weibull.yaml");

    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        GenericOptimization optimization = yaml.loadAs(new FileReader(CALIBRATION_FILE.toFile()),
                                                              GenericOptimization.class);


        Scenario bestScenario = GenericOptimization.buildScenario(
                BEST_SOLUTION,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
        );
        ((ExogenousFadSetterCSVFactory) ((EpoFadsOnlyAbundanceScenario) bestScenario).getFadSetterFactory()).setKeepLog(true);

        yaml.dump(bestScenario,new FileWriter(CALIBRATION_FILE.getParent().resolve("best_scenario.yaml").toFile()));

        FishStateUtilities.run("logged",CALIBRATION_FILE.getParent().resolve("best_scenario.yaml"),
                               CALIBRATION_FILE.getParent().resolve("best_run"),
                               0L,
                               0,
                               false,
                               null,2,
                               false,
                               -1,null,null,null,null);


    }


}
