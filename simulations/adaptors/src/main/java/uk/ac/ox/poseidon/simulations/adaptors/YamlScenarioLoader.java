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

package uk.ac.ox.poseidon.simulations.adaptors;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.poseidon.common.core.Services;
import uk.ac.ox.poseidon.simulations.api.FileScenarioLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@AutoService(FileScenarioLoader.class)
public class YamlScenarioLoader implements FileScenarioLoader {
    private static final Set<String> SUPPORTED_EXTENSIONS = ImmutableSet.of("yaml");

    @Override
    public uk.ac.ox.poseidon.simulations.api.Scenario load(final Path scenarioPath) {
        try (final FileInputStream fileInputStream = new FileInputStream(scenarioPath.toFile())) {
            final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_8);
            final Scenario scenario = new FishYAML().loadAs(inputStreamReader, Scenario.class);
            return Services.loadAdaptorFactory(ScenarioAdaptorFactory.class, Scenario.class).apply(scenario);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioPath, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioPath, e);
        }
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
}
