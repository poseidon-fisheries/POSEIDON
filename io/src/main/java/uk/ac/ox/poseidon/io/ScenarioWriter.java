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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.utils.CustomPathConverter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Serializes a {@link Scenario} to YAML, either as a string or written directly to a file/stream.
 * The counterpart of {@link ScenarioLoader}; also runnable as a CLI ({@code main}) that
 * instantiates a named {@code Supplier<Scenario>} class and writes its result to a given path —
 * this is how the generated scenario files under {@code inputs/} are produced, never by hand-
 * editing the YAML.
 */
public class ScenarioWriter {
    private final Yaml yaml;

    /**
     * Constructs a {@code ScenarioWriter} with default {@link DumperOptions} (pretty flow-style).
     */
    public ScenarioWriter() {
        this(new Yaml(defaultDumperOptions()));
    }

    /** @param yaml the configured {@link Yaml} instance to dump scenarios through */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ScenarioWriter(final Yaml yaml) {
        this.yaml = yaml;
    }

    private static DumperOptions defaultDumperOptions() {
        final DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        return dumperOptions;
    }

    /**
     * @param scenario the scenario to serialize
     * @return the scenario as a YAML string
     */
    public String write(final Scenario scenario) {
        return yaml.dump(scenario);
    }

    /**
     * @param scenario the scenario to serialize
     * @param filePath the path of the file to write the YAML to
     * @throws RuntimeException if an I/O error occurs
     */
    public void write(
        final Scenario scenario,
        final Path filePath
    ) {
        write(scenario, filePath.toFile());
    }

    /**
     * @param scenario the scenario to serialize
     * @param file     the file to write the YAML to
     * @throws RuntimeException if an I/O error occurs
     */
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

    /**
     * @param scenario the scenario to serialize
     * @param writer   the writer to dump the YAML to
     */
    public void write(
        final Scenario scenario,
        final Writer writer
    ) {
        yaml.dump(scenario, writer);
    }

    private static class Args {

        @Parameter(
            names = {"-c", "--scenario_class_name"},
            description = "Name of the scenario supplier class",
            required = true
        )
        private String scenarioSupplierClassName;

        @Parameter(
            names = {"-s", "--scenario_path"},
            description = "Path to the scenario file in YAML format.",
            converter = CustomPathConverter.class,
            required = true
        )
        private Path scenarioPath;

    }

    static void main(final String[] args) {
        final Args writerArgs = new ScenarioWriter.Args();
        JCommander.newBuilder().addObject(writerArgs).build().parse(args);
        final Scenario scenario;
        try {
            final Class<?> clazz = Class.forName(writerArgs.scenarioSupplierClassName);
            if (!Supplier.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                    "Class " + clazz.getName() + " does not implement Supplier<Scenario>."
                );
            }
            final Supplier<?> supplier =
                clazz.asSubclass(Supplier.class).getDeclaredConstructor().newInstance();
            final Object object = supplier.get();
            if (!(object instanceof Scenario)) {
                throw new IllegalArgumentException(
                    "Supplier class " + clazz.getName() + " does not return a Scenario."
                );
            }
            scenario = (Scenario) object;
        } catch (
            final ClassNotFoundException | InvocationTargetException | InstantiationException |
                  IllegalAccessException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
        final ScenarioWriter scenarioWriter = new ScenarioWriter();
        scenarioWriter.write(scenario, writerArgs.scenarioPath);
    }
}
