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
 *
 */

package uk.ac.ox.poseidon.io;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.poseidon.core.Scenario;

import java.io.*;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ScenarioWriter {
    private final Yaml yaml;

    public ScenarioWriter() {
        this(new Yaml(defaultDumperOptions()));
    }

    public ScenarioWriter(final Yaml yaml) {
        this.yaml = yaml;
    }

    private static DumperOptions defaultDumperOptions() {
        final DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        return dumperOptions;
    }

    public String write(final Scenario scenario) {
        return yaml.dump(scenario);
    }

    public void write(
        final Scenario scenario,
        final Path filePath
    ) {
        write(scenario, filePath.toFile());
    }

    public void write(
        final Scenario scenario,
        final File file
    ) {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file, UTF_8))) {
            write(scenario, writer);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(
        final Scenario scenario,
        final Writer writer
    ) {
        yaml.dump(scenario, writer);
    }
}
