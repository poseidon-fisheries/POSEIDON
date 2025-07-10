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

package uk.ac.ox.poseidon.io.tables;

import lombok.RequiredArgsConstructor;
import sim.engine.SimState;
import sim.engine.Steppable;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.INFO;

@RequiredArgsConstructor
public class CsvTableWriter implements Steppable {

    private static final Logger logger = System.getLogger(CsvTableWriter.class.getName());

    private final Supplier<Table> tableSupplier;
    private final Path path;
    private final boolean append;
    private final boolean clearAfterWriting;

    @Override
    public void step(final SimState state) {
        final boolean fileExists = path.toFile().exists();
        try (
            final var fos = new FileOutputStream(path.toFile(), append);
            final var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            final var bw = new BufferedWriter(osw)
        ) {
            final Table table = tableSupplier.get();
            table.write().csv(CsvWriteOptions.builder(bw).header(!fileExists).build());
            if (clearAfterWriting) {
                table.clear();
            }
            logger.log(INFO, "Written " + path.toAbsolutePath());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
