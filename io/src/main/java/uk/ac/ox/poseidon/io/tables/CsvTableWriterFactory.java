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

import lombok.*;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

import java.nio.file.Path;
import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CsvTableWriterFactory extends SimulationScopeFactory<CsvTableWriter> {

    private Factory<? extends Supplier<Table>> tableSupplier;
    private Factory<? extends Path> path;
    private boolean clearAfterWriting;

    @Override
    protected CsvTableWriter newInstance(final @NonNull Simulation simulation) {
        return new CsvTableWriter(
            tableSupplier.get(simulation),
            path.get(simulation),
            clearAfterWriting
        );
    }
}
