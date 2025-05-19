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

package uk.ac.ox.oxfish.utility.yaml;

import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class ScenarioUpdater {

    public static void updateScenario(
        final Path inputScenario,
        final Path outputScenario,
        final Function<Stream<String>, String> lineProcessor,
        final Consumer<Scenario> scenarioConsumer
    ) {
        System.out.print("===\n" + inputScenario + "\n===\n");
        try (final Stream<String> scenarioLines = Files.lines(inputScenario)) {

            final String scenarioYaml = lineProcessor.apply(scenarioLines);
            System.out.println(scenarioYaml);
            final FishYAML fishYAML = new FishYAML();
            final Scenario scenario = fishYAML.loadAs(scenarioYaml, Scenario.class);
            scenarioConsumer.accept(scenario);
            // scenario.getCatchSamplersFactory().setSpeciesCodesSupplier(scenario.getSpeciesCodesSupplier());
            try (final FileOutputStream fileOutputStream = new FileOutputStream(outputScenario.toFile())) {
                final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                fishYAML.dump(scenario, outputStreamWriter);
            }
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + inputScenario, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + inputScenario, e);
        }
    }

}
