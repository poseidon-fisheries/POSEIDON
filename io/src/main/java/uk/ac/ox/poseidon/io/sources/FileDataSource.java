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

package uk.ac.ox.poseidon.io.sources;

import lombok.Value;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * A {@link DataSource} over a plain file, read with a fixed {@link Charset}. Built via
 * {@link Factories#fileDataSource(uk.ac.ox.poseidon.core.Factory)}/
 * {@link Factories#fileDataSource(uk.ac.ox.poseidon.core.Factory, String)} in this package.
 */
@Value
public class FileDataSource implements DataSource {

    File file;
    Charset charset;

    @Override
    public Reader getReader() {
        try {
            return Files.newBufferedReader(file.toPath(), charset);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
