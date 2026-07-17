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

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.functions.ComposedFunctionFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.utils.ListFactory;
import uk.ac.ox.poseidon.io.sources.DataSource;
import uk.ac.ox.poseidon.io.sources.FileDataSourceFactory;
import uk.ac.ox.poseidon.io.sources.StringDataSourceFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.functions.Factories.composedFunction;
import static uk.ac.ox.poseidon.core.functions.Factories.mapValueExtractor;

public class Factories {

    private Factories() {
    }

    public static CsvTableFactory<Scope> csvTableFromString(final String data) {
        return new CsvTableFactory<>(new StringDataSourceFactory(data));
    }

    public static <S extends Scope> CsvTableFactory<S> csvTableFromFile(
        final Factory<S, ? extends Path> pathFactory
    ) {
        return new CsvTableFactory<>(new FileDataSourceFactory<>(pathFactory));
    }

    public static <S extends Scope> CsvTableFactory<S> csvTableFrom(
        final Factory<S, ? extends DataSource> dataSourceFactory
    ) {
        return new CsvTableFactory<>(dataSourceFactory);
    }

    public static <S extends Scope> CsvTableWriterFactory<S> csvTableWriter(
        final Factory<? super S, ? extends Supplier<Table>> tableSupplier,
        final Factory<? super S, ? extends Path> path,
        final boolean append,
        final boolean clearAfterWriting
    ) {
        return new CsvTableWriterFactory<>(tableSupplier, path, append, clearAfterWriting);
    }

    public static <S extends Scope, K, V> TableFromMapFactory<S, K, V> tableFromMap(
        final Factory<? super S, ? extends TableDefinition> tableDefinition,
        final Factory<? super S, ? extends Supplier<Map<K, V>>> mapSupplier
    ) {
        return new TableFromMapFactory<>(
            tableDefinition,
            mapSupplier
        );
    }

    @SafeVarargs
    public static <C extends Supplier<?>> SteppableTableFactory steppableTable(
        final Factory<? super SimulationScope, TableDefinition> tableDefinition,
        final Factory<? super SimulationScope, ? extends C>... valueSuppliers
    ) {
        return new SteppableTableFactory(
            tableDefinition,
            new ListFactory<SimulationScope, C>(List.of(valueSuppliers))
        );
    }

    public static SteppableTableFactory steppableTable(
        final Factory<? super SimulationScope, TableDefinition> tableDefinition,
        final Factory<? super SimulationScope, List<? extends Supplier<?>>> valueSuppliers
    ) {
        return new SteppableTableFactory(tableDefinition, valueSuppliers);
    }

    public static ColumnDefinitionFactory columnDefinition(
        final String columnName,
        final String columnType
    ) {
        return new ColumnDefinitionFactory(columnName, columnType);
    }

    @SafeVarargs
    public static <S extends Scope, C extends ColumnDefinition> TableDefinitionFactory<S> tableDefinition(
        final Factory<? super S, C>... columnDefinitions
    ) {
        return tableDefinition(new ListFactory<S, C>(List.of(columnDefinitions)));
    }

    public static <S extends Scope> TableDefinitionFactory<S> tableDefinition(
        final Factory<? super S, ? extends List<? extends ColumnDefinition>> columnDefinitions
    ) {
        return new TableDefinitionFactory<>(columnDefinitions);
    }

    public static <S extends Scope, K, V> MapFromTableFactory<S, K, V> mapFromTable(
        final Factory<? super S, Table> table,
        final Factory<? super S, ? extends Function<? super Row, ? extends K>> keyBuilder,
        final Factory<? super S, ? extends Function<? super Row, ? extends V>> valueBuilder
    ) {
        return new MapFromTableFactory<>(table, keyBuilder, valueBuilder);
    }

    public static MultiStringKeyFromRowFactory multiStringKeyFromRow(final String... keyColumnNames) {
        return new MultiStringKeyFromRowFactory(Arrays.asList(keyColumnNames));
    }

    public static DoubleFromRowFactory doubleFromRow(final String columnName) {
        return new DoubleFromRowFactory(columnName);
    }

    public static <S extends Scope, T, K, V> ComposedFunctionFactory<S, T, K, V> tableLookup(
        final Factory<? super S, ? extends Function<? super T, ? extends K>> keyExtractor,
        final Factory<? super S, Table> table,
        final Factory<? super S, ? extends Function<? super Row, ? extends K>> rowKeyBuilder,
        final Factory<? super S, ? extends Function<? super Row, ? extends V>> rowValueBuilder
    ) {
        return composedFunction(
            keyExtractor,
            mapValueExtractor(mapFromTable(table, rowKeyBuilder, rowValueBuilder))
        );
    }
}
