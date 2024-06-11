/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.scenarios;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Paths;

class EpoScenarioTest {

    @Test
    void testSaveAndLoadEpoGravityAbundanceScenario() {
        saveAndLoadYaml(EpoGravityAbundanceScenario.class);
    }

    <S extends EpoScenario<?>> void saveAndLoadYaml(
        final Class<S> scenarioClass
    ) {
        try {
            final S constructedScenario = scenarioClass.newInstance();
            final File scenarioFile = constructedScenario.testFolder().get()
                .resolve(Paths.get("scenarios", scenarioClass.getSimpleName() + ".yaml"))
                .toFile();
            new FishYAML().dump(constructedScenario, new FileWriter(scenarioFile));
            // Try to read it back and start it
            try (final FileReader fileReader = new FileReader(scenarioFile)) {
                final FishYAML fishYAML = new FishYAML();
                final EpoScenario<?> loadedScenario = fishYAML.loadAs(
                    fileReader,
                    EpoScenario.class
                );
                loadedScenario.useDummyData();
                final FishState fishState = new FishState();
                fishState.setScenario(loadedScenario);
                fishState.start();
            } catch (final FileNotFoundException e) {
                throw new IllegalArgumentException("Can't find scenario file: " + scenarioFile, e);
            } catch (final IOException e) {
                throw new IllegalStateException("Error while reading file: " + scenarioFile, e);
            }
        } catch (final IOException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void testSaveAndLoadEpoGravityBiomassScenario() {
        saveAndLoadYaml(EpoGravityBiomassScenario.class);
    }

    @Test
    void testSaveAndLoadEpoFadsOnlyAbundanceScenario() {
        saveAndLoadYaml(EpoFadsOnlyAbundanceScenario.class);
    }

    @Test
    void testSaveAndLoadEpoAbundanceScenario() {
        saveAndLoadYaml(EpoAbundanceScenario.class);
    }

    @Test
    void testSaveAndLoadEpoPathPlannerAbundanceScenario() {
        saveAndLoadYaml(EpoPathPlannerAbundanceScenario.class);
    }

}
