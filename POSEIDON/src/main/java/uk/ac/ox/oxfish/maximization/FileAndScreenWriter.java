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

package uk.ac.ox.oxfish.maximization;

import eva2.optimization.statistics.InterfaceTextListener;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileAndScreenWriter implements InterfaceTextListener, Closeable {

    private final FileWriter fileWriter;

    public FileAndScreenWriter(final Path outputFile) throws IOException {
        this.fileWriter = new FileWriter(outputFile.toFile());
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }

    @Override
    public void println(final String str) {
        print(str + "\n");
    }

    @Override
    public void print(final String str) {
        System.out.println(str);
        try {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
