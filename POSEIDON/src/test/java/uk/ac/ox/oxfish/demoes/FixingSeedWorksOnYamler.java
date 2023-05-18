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

package uk.ac.ox.oxfish.demoes;

import org.junit.After;
import org.junit.Test;
import uk.ac.ox.oxfish.YamlMain;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 3/16/16.
 */
public class FixingSeedWorksOnYamler {


    @Test
    public void fixingSeedWorksOnYamler() throws Exception {

        Path firstInputPath = Paths.get("inputs", "tests", "replicate.yaml");
        Path secondInputPath = Paths.get("inputs", "tests", "replicate2.yaml");
        String firstInput = String.join("\n", Files.readAllLines(firstInputPath));
        String secondInput = String.join("\n", Files.readAllLines(secondInputPath));
        assertEquals(firstInput, secondInput);

        YamlMain.main(new String[]{firstInputPath.toString(), "--seed", "567", "--years", "3"});
        YamlMain.main(new String[]{secondInputPath.toString(), "--seed", "567", "--years", "3"});

        String firstOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate/result.yaml")));
        String secondOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate2/result.yaml")));
        assertEquals(firstOutput, secondOutput);

        FishStateUtilities.deleteRecursively(Paths.get("output", "replicate").toFile());
        FishStateUtilities.deleteRecursively(Paths.get("output", "replicate2").toFile());


    }

    @Test
    public void policyScriptWorksAsGodIntended() throws Exception {
        Path inputPath = Paths.get("inputs", "tests", "replicate.yaml");
        Path policyPath = Paths.get("inputs", "tests", "policy_script.yaml");
        YamlMain.main(new String[]{inputPath.toString(), "--years", "3", "--policy", policyPath.toString(), "--save"});

        //lspiRun the model, save it and then read it back so that we test checkpointing too.
        FishState state = FishStateUtilities.readModelFromFile(
            Paths.get("output", "replicate", "replicate.checkpoint").toFile());

        assertEquals(state.getFishers().size(), 15);
        //should there be no fishing on year 1, however some people might return to port from previous day fishing
        //so that while gear effectivness is 0, landings is some small hundreds
        assertTrue(state.getYearlyDataSet().getColumn("Species 0 Landings").get(1) <
            state.getYearlyDataSet().getColumn("Species 0 Landings").get(0) * .1);
        assertTrue(state.getYearlyDataSet().getColumn("Species 0 Landings").get(1) <
            state.getYearlyDataSet().getColumn("Species 0 Landings").get(2) * .1);
        assertTrue(state.getYearlyDataSet().getColumn("Species 0 Landings").get(2) > 1000d);

        FishStateUtilities.deleteRecursively(Paths.get("output", "replicate").toFile());

    }

    @After
    public void tearDown() throws Exception {

        File[] filesInOutputFolder = Paths.get("output").toFile().listFiles();
        if (filesInOutputFolder == null || filesInOutputFolder.length == 0)
            Paths.get("output").toFile().delete();
    }
}
