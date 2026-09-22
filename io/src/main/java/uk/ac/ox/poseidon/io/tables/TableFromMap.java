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
import tech.tablesaw.columns.Column;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link Supplier} that builds a two-column {@code tablesaw} {@link Table} (key, value) from a
 * resolved {@link Map}, using a fixed {@link TableDefinition} for the table shape. Built via
 * {@link Factories#tableFromMap(uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory)}
 * in this package.
 *
 * @param <K> the type of the map's keys, written to the table's first column
 * @param <V> the type of the map's values, written to the table's second column
 */
@RequiredArgsConstructor
public class TableFromMap<K, V> implements Supplier<Table> {

    private final TableDefinition tableDefinition;
    private final Supplier<Map<K, V>> mapSupplier;

    /**
     * @return a fresh {@link Table}, shaped by {@code tableDefinition}, with one row per entry of
     * the resolved map
     */
    @Override
    public Table get() {
        final Map<K, V> map = mapSupplier.get();
        final Table table = tableDefinition.get();
        final Column<?> keyColumn = table.column(0);
        final Column<?> valueColumn = table.column(1);
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            keyColumn.appendObj(entry.getKey());
            valueColumn.appendObj(entry.getValue());
        }
        return table;
    }
}
