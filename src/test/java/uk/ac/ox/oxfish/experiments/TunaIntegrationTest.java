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
}
