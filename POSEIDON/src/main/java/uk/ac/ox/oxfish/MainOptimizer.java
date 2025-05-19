/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish;


import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.oxfish.maximization.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class MainOptimizer {


    static {

        Yaml yaml = new Yaml();
        try {
            yaml.dump(
                new SamplePoseidonOptimization(),
                new FileWriter(Paths.get("eva", "sample.yaml").toFile())
            );
            yaml.dump(
                new CaliforniaDerisoOptimization(),
                new FileWriter(Paths.get("eva", "deriso.yaml").toFile())
            );
            yaml.dump(
                new OneGearExampleMaximization(),
                new FileWriter(Paths.get("eva", "example.yaml").toFile())
            );
            yaml.dump(
                new MultipleGearsExampleMaximization(),
                new FileWriter(Paths.get("eva", "flexible.yaml").toFile())
            );
            yaml.dump(
                new GenericOptimization(),
                new FileWriter(Paths.get("eva", "generic.yaml").toFile())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {


        eva2.gui.Main.main(args);

    }
}
