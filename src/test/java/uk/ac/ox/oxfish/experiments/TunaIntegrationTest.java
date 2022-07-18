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

package uk.ac.ox.oxfish.experiments;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.TunaEvaluator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TunaIntegrationTest {


    //makes sure that whatever we change later on, we can still get the old numbers back somehow
    @Test
    public void pathfinderBacksliding() throws IOException {

        double[] solution =
                {-51.724, 22.532, 21.767,-63.344,-3.947,-72.007, 25.182, 695.676, 0.686, 33.917, 14.578, 0.479,-25.794, 0.229, 25.667, 38.437,-27.530, 13.663, 37.790,-1.460, 3.542, 4.050,-0.639, 35.812, 118.978,-15.255, 24.295};

        Path calibrationFile = Paths.get(
                "inputs/tests/tunabacksliding/zapper_local_expired.yaml"
        );
        GenericOptimization optimization = GenericOptimization.fromFile(calibrationFile);
        double[] evaluate = optimization.evaluate(solution);
        System.out.println(evaluate[0]);
        Assert.assertTrue(evaluate[0]<120);



    }
    //    @Test
//    public void lastMomentBacksliding() throws IOException {
//
//        double[] solution =
//                {-6.615,-3.512, 9.033,-3.895, 2.017, 10.000,-6.421,-0.553, 0.210, 4.370, 4.600,-2.585, 6.533, 9.977,-9.485};
//
//
//        new TunaEvaluator(Paths.get(
//                "docs/20220223 tuna_calibration/pathfinder_lastmoment/cenv0477/2022-06-15_09.59.31_noreach2_bias/test_with_range/newdata/linearcatchability_noareach2_bias.yaml"),
//                          solution).run();
//
//
//
//
//
//    }
    @Test
    public void logisticBacksliding() throws IOException {

        double[] solution =
                {
                        -51.979, 22.402,-21.136,-18.328,-3.078,-8.661,-199.316, 165.836, 2.330, 11.756,-48.978,-4.760,-1.190,-4.217,-45.867,-32.822, 14.977,-7.667,-58.219, 2.365, 2.720, 3.929,-95.199,-15.215, 57.180,-24.763, 15.735, 3.249,-25.388
                };



        Path calibrationFile = Paths.get(
                "inputs/tests/tunabacksliding/calibration_logistic.yaml"
        );
        GenericOptimization optimization = GenericOptimization.fromFile(calibrationFile);
        double[] evaluate = optimization.evaluate(solution);
        System.out.println(evaluate[0]);
        Assert.assertTrue(evaluate[0]<120);





    }
}
