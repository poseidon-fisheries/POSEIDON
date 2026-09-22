/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import sim.engine.SimState;
import sim.engine.Steppable;
import tech.tablesaw.api.Table;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serial;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link Table} that grows one row per simulation step: each {@link #step} call appends the
 * current values of a fixed list of per-column {@link Supplier}s. Built via
 * {@link Factories#steppableTable(uk.ac.ox.poseidon.core.Factory,
 * uk.ac.ox.poseidon.core.Factory[])} in this package.
 */
public class SteppableTable implements Steppable, Supplier<Table> {
    @Serial private static final long serialVersionUID = 1L;

    private final transient Table table;
    private final List<? extends Supplier<?>> valueSuppliers;

    /**
     * @param tableDefinition the shape of the table to create
     * @param valueSuppliers  one value supplier per column, in column order
     * @throws IllegalArgumentException if the number of suppliers doesn't match the number of
     *                                   columns in {@code tableDefinition}
     */
    public SteppableTable(
        final TableDefinition tableDefinition,
        final List<? extends Supplier<?>> valueSuppliers
    ) {
        this.table = tableDefinition.get();
        checkArgument(table.columnCount() == valueSuppliers.size());
        this.valueSuppliers = List.copyOf(valueSuppliers);
    }

    /** @return the underlying, growing {@link Table} */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Table get() {
        return table;
    }

    /** Appends one row: the current value of each of {@code valueSuppliers}, in column order. */
    @Override
    public void step(final SimState simState) {
        for (int i = 0; i < valueSuppliers.size(); i++) {
            table.column(i).appendObj(valueSuppliers.get(i).get());
        }
    }
}
