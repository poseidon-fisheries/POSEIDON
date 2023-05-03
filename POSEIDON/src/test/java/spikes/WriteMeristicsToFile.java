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

package spikes;

import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class WriteMeristicsToFile {

    public void toWrite() throws Exception {

        HashMap<String,MeristicsInput> inputs = new HashMap<>();

        inputs.put("Yelloweye Rockfish",
                   new MeristicsInput(100,70, 1, 18.717, 64.594, 0.047, 0.000017, 3.03,
                                      0.045, 1, 18.717, 62.265, 0.047, 0.00000977, 3.17,
                                      0.046, 38.78, -0.437, 137900, 36500,
                                      228149,
                                      0.44056, true));

        inputs.put("Shortspine Thornyhead",
                   new MeristicsInput(100, 100, 2, 7, 75, 0.018, 4.77E-06, 3.263,
                                      0.0505, 2, 7, 75, 0.018, 4.77E-06, 3.263,
                                      0.0505, 18.2, -2.3, 1, 0, 36315502,
                                      0.6, false));


        inputs.put("Longspine Thornyhead",
                   new MeristicsInput(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                      0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                      0.111313, 17.826, -1.79, 1,
                                      0, 168434124,
                                      0.6, false));

        inputs.put("Sablefish",
                   new MeristicsInput(59,30 , 0.5, 25.8, 56.2, 0.419, 3.6724E-06, 3.250904,
                                      0.065, 0.5, 25.8, 64, 0.335, 3.4487E-06, 3.26681,
                                      0.08, 58, -0.13, 1, 0, 40741397,
                                      0.6, false));

        inputs.put("Dover Sole",
                   new MeristicsInput(69,50 , 1, 9.04, 39.91, 0.1713, 0.000002231, 3.412,
                                      0.1417, 1, 5.4, 47.81, 0.1496, 0.000002805, 3.345,
                                      0.1165, 35, -0.775, 1, 0,
                                      404138330,
                                      0.8, false));

        inputs.put("Canary Rockfish",
                   new MeristicsInput(40,20 , 1, 8.04, 52.53, 0.16, 1.55E-05, 3.03,
                                      0.06, 1, 8.04, 60.36, 0.125, 1.55E-05, 3.03,
                                      0.06, 40.5, -0.25, 1, 0,
                                      38340612,
                                      0.511, true));


        FishYAML yaml = new FishYAML();
        Path mainPath = Paths.get("inputs", "california", "biology");
        for(Map.Entry<String,MeristicsInput> input : inputs.entrySet())
        {
            Path speciesDirectory = mainPath.resolve(input.getKey());
            speciesDirectory.toFile().mkdirs();
            String dumped = yaml.dumpAsMap(input.getValue());
            FileWriter writer = new FileWriter(speciesDirectory.resolve("meristics.yaml").toFile());
            writer.write(dumped);
            writer.flush();
            writer.close();

        }

    }
}
