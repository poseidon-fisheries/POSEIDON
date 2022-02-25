/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import static uk.ac.ox.oxfish.geography.fads.ExogenousFadSetter.initFadRemovalLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

public class FadsOnlyEpoAbundanceScenarioTest extends TestCase {

    public void testSaveAndLoadYaml() {

        // Dump the scenario to YAML
        final File scenarioFile = Paths.get("inputs", "tests", "fad_only_scenario.yaml").toFile();

        try {
            final Scenario scenario = new FadsOnlyEpoAbundanceScenario();
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        // Try to read it back and start it
        try (final FileReader fileReader = new FileReader(scenarioFile)) {
            final FishYAML fishYAML = new FishYAML();
            final FadsOnlyEpoAbundanceScenario epoAbundanceScenario =
                fishYAML.loadAs(fileReader, FadsOnlyEpoAbundanceScenario.class);
            final FishState fishState = new FishState();
            fishState.setScenario(epoAbundanceScenario);
            fishState.start();
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioFile, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioFile, e);
        }

    }

    public void testRunTwoYearsWithoutCrashing() {
        final Scenario scenario = new FadsOnlyEpoAbundanceScenario();
        final FishState fishState = new FishState();
        initFadRemovalLog();
        fishState.setScenario(scenario);
        fishState.start();
        do {
            fishState.schedule.step(fishState);
            System.out.println("Step " + fishState.getStep());
            System.out.println(
                fishState.getFadMap().getDriftingObjectsMap().getField().allObjects.numObjs
            );
        } while (fishState.getYear() < 2);
    }

}