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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.poseidon.core.Scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The {@code ScenarioLoader} class provides methods to load {@code Scenario} objects from YAML
 * strings, files, and input streams. This class uses the SnakeYAML library to parse YAML data.
 */
public class ScenarioLoader {

    private static final int MAX_ALIASES_FOR_COLLECTIONS = 5000;
    private final Yaml yaml;

    /**
     * Constructs a {@code ScenarioLoader} with a default {@code Yaml} instance.
     */
    public ScenarioLoader() {
        this(new Yaml(getLoaderOptions()));
    }

    /**
     * Constructs a {@code ScenarioLoader} with a specified {@code Yaml} instance.
     *
     * @param yaml the {@code Yaml} instance to use for loading scenarios
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ScenarioLoader(final Yaml yaml) {
        this.yaml = yaml;
    }

    private static LoaderOptions getLoaderOptions() {
        final LoaderOptions options = new LoaderOptions();
        options.setTagInspector(tag ->
            tag.getClassName().startsWith("uk.ac.ox.poseidon") ||
                tag.getClassName().startsWith("java")
        );
        options.setMaxAliasesForCollections(MAX_ALIASES_FOR_COLLECTIONS);
        return options;
    }

    /**
     * Loads a {@code Scenario} from a YAML string.
     *
     * @param yamlString the YAML string containing the scenario data
     * @return the loaded {@code Scenario} object
     */
    public Scenario load(final String yamlString) {
        return yaml.loadAs(yamlString, Scenario.class);
    }

    /**
     * Loads a {@code Scenario} from a file.
     *
     * @param file the file containing the YAML data
     * @return the loaded {@code Scenario} object
     * @throws RuntimeException if an I/O error occurs
     */
    public Scenario load(final File file) {
        try (final InputStream inputStream = new FileInputStream(file)) {
            return load(inputStream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a {@code Scenario} from an input stream.
     *
     * @param inputStream the input stream containing the YAML data
     * @return the loaded {@code Scenario} object
     */
    public Scenario load(final InputStream inputStream) {
        return yaml.load(inputStream);
    }
}
