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

package uk.ac.ox.poseidon.io.tables;

import lombok.RequiredArgsConstructor;
import sim.engine.SimState;
import sim.engine.Steppable;
import tech.tablesaw.api.Table;

import java.nio.file.Path;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class CsvTableWriter implements Steppable {

    private final Supplier<Table> tableSupplier;
    private final Path path;
    private final boolean clearAfterWriting;

    @Override
    public void step(final SimState state) {
        final Table table = tableSupplier.get();
        table.write().csv(path.toFile());
        if (clearAfterWriting) {
            table.clear();
        }
    }
}
