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

import static java.util.function.UnaryOperator.identity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

public class ScenarioTestUtils {

    public static <S extends TestableScenario> void testSaveAndLoadYaml(
        final Path testFolder,
        final String scenarioFileName,
        final Class<S> scenarioClass
    ) {
        testSaveAndLoadYaml(testFolder, scenarioFileName, scenarioClass, identity());
    }

    public static <S extends TestableScenario> void testSaveAndLoadYaml(
        final Path testFolder,
        final String scenarioFileName,
        final Class<S> scenarioClass,
        final UnaryOperator<S> scenarioOperator
    ) {
        // Dump the scenario to YAML
        final File scenarioFile = testFolder.resolve(scenarioFileName).toFile();
        try {
            final Scenario scenario = scenarioOperator.apply(scenarioClass.newInstance());
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
        } catch (final IOException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        // Try to read it back and start it
        try (final FileReader fileReader = new FileReader(scenarioFile)) {
            final FishYAML fishYAML = new FishYAML();
            final S scenario = fishYAML.loadAs(fileReader, scenarioClass);
            scenario.useDummyData();
            final FishState fishState = new FishState();
            fishState.setScenario(scenario);
            fishState.start();
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioFile, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioFile, e);
        }

    }


}
