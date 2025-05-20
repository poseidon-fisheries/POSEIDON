/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.io;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.poseidon.core.Scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The {@code ScenarioLoader} class provides methods to load {@code Scenario} objects from YAML
 * strings, files, and input streams. This class uses the SnakeYAML library to parse YAML data.
 */
public class ScenarioLoader {

    private static final List<String> DEFAULT_CLASS_PREFIXES =
        List.of("uk.ac.ox.poseidon", "java");
    private static final int MAX_ALIASES_FOR_COLLECTIONS = 5000;
    private final Yaml yaml;

    /**
     * Constructs a {@code ScenarioLoader} with a default {@code Yaml} instance, optionally passing
     * class prefixes that will be permitted in addition to "uk.ac.ox.poseidon" and "java" when
     * loading factory classes from the YAML scenario.
     */
    public ScenarioLoader(final String... extraClassPrefixes) {
        this(new Yaml(getLoaderOptions(Arrays.asList(extraClassPrefixes))));
    }

    /**
     * Constructs a {@code ScenarioLoader} with a default {@code Yaml} instance, passing class
     * prefixes that will be permitted in addition to "uk.ac.ox.poseidon" and "java" when loading
     * factory classes from the YAML scenario.
     */
    public ScenarioLoader(final Collection<String> extraClassPrefixes) {
        this(new Yaml(getLoaderOptions(extraClassPrefixes)));
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

    private static LoaderOptions getLoaderOptions(final Collection<String> extraClassPrefixes) {
        final LoaderOptions options = new LoaderOptions();
        final ImmutableList<String> permittedClassPrefixes = ImmutableList
            .<String>builder()
            .addAll(DEFAULT_CLASS_PREFIXES)
            .addAll(extraClassPrefixes)
            .build();
        options.setTagInspector(tag ->
            permittedClassPrefixes
                .stream()
                .anyMatch(prefix -> tag.getClassName().startsWith(prefix))
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
