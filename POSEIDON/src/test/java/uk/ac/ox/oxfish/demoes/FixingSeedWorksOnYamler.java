/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.demoes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.YamlMain;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 3/16/16.
 */
public class FixingSeedWorksOnYamler {


    @Test
    public void fixingSeedWorksOnYamler() throws Exception {

        final Path firstInputPath = Paths.get("inputs", "tests", "replicate.yaml");
        final Path secondInputPath = Paths.get("inputs", "tests", "replicate2.yaml");
        final String firstInput = String.join("\n", Files.readAllLines(firstInputPath));
        final String secondInput = String.join("\n", Files.readAllLines(secondInputPath));
        Assertions.assertEquals(firstInput, secondInput);

        YamlMain.main(new String[]{firstInputPath.toString(), "--seed", "567", "--years", "3"});
        YamlMain.main(new String[]{secondInputPath.toString(), "--seed", "567", "--years", "3"});

        final String firstOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate/result.yaml")));
        final String secondOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate2/result.yaml")));
        Assertions.assertEquals(firstOutput, secondOutput);

        FishStateUtilities.deleteRecursively(Paths.get("output", "replicate").toFile());
        FishStateUtilities.deleteRecursively(Paths.get("output", "replicate2").toFile());
    }

    @AfterEach
    public void tearDown() throws Exception {

        final File[] filesInOutputFolder = Paths.get("output").toFile().listFiles();
        if (filesInOutputFolder == null || filesInOutputFolder.length == 0)
            Paths.get("output").toFile().delete();
    }
}
