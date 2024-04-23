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
package uk.ac.ox.poseidon.common.core.yaml;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class YamlLoader<T> {
    private final Class<? extends T> type;

    public YamlLoader(final Class<? extends T> type) {
        this.type = type;
    }

    public T load(final Path yamlFile) {
        return load(yamlFile.toFile());
    }

    public T load(final File yamlFile) {
        final T object;
        try (final FileReader fileReader = new FileReader(yamlFile)) {
            object = new Yaml().loadAs(fileReader, type);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return object;
    }
}
