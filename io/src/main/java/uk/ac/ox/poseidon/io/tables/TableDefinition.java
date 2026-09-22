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

import lombok.RequiredArgsConstructor;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.function.Supplier;

/**
 * A named, typed {@code tablesaw} {@link Table} shape that can be created (empty) on demand, from
 * a fixed list of {@link ColumnDefinition}s. Built via {@code Factories.tableDefinition(...)} in
 * this package.
 */
@RequiredArgsConstructor
public class TableDefinition implements Supplier<Table> {

    private final List<? extends ColumnDefinition> columnDefinitions;

    /** @return a fresh, empty {@link Table} with one column per {@code columnDefinitions} entry */
    @Override
    public Table get() {
        return Table.create(columnDefinitions.stream().map(ColumnDefinition::get));
    }

}
