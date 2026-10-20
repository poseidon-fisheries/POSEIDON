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

import static uk.ac.ox.poseidon.core.functions.Factories.mapLookup;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MapLookupFactory;
import uk.ac.ox.poseidon.core.functions.MappingAsValueFactory;
import uk.ac.ox.poseidon.core.utils.ListFactory;
import uk.ac.ox.poseidon.io.sources.DataSource;
import uk.ac.ox.poseidon.io.sources.FileDataSourceFactory;
import uk.ac.ox.poseidon.io.sources.StringDataSourceFactory;

/**
 * Factories for {@code tablesaw} {@link Table} components: building tables from
 * CSV or from a resolved {@link Map}, writing them back out to CSV, growing one
 * incrementally over the course of a simulation, and deriving a lookup from a
 * table's contents.
 */
@SuppressWarnings({ "overloads", "varargs" })
public class Factories {

  private Factories() {}

  /**
   * @param data the literal CSV content
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link Table} parsed from {@code data}
   * @see TableFromCsvFactory
   */
  public static TableFromCsvFactory<Scope> tableFromCsvString(
    final String data
  ) {
    return new TableFromCsvFactory<>(new StringDataSourceFactory(data));
  }

  /**
   * @param pathFactory factory for the CSV file to read
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link Table} parsed from the resolved file
   * @see TableFromCsvFactory
   */
  public static <S extends Scope> TableFromCsvFactory<S> tableFromCsvFile(
    final Factory<S, ? extends Path> pathFactory
  ) {
    return new TableFromCsvFactory<>(new FileDataSourceFactory<>(pathFactory));
  }

  /**
   * @param dataSourceFactory factory for the CSV content to read
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link Table} parsed from the resolved data source
   * @see TableFromCsvFactory
   */
  public static <S extends Scope> TableFromCsvFactory<S> tableFromCsv(
    final Factory<S, ? extends DataSource> dataSourceFactory
  ) {
    return new TableFromCsvFactory<>(dataSourceFactory);
  }

  /**
   * @param tableSupplier     factory for the supplier of the table to write on
   *                          each step
   * @param path              factory for the CSV file to write to
   * @param append            whether to append to an existing file rather than
   *                          overwrite it
   * @param clearAfterWriting whether to clear the table's rows immediately
   *                          after each write
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link CsvTableWriter}
   * @see CsvTableWriter
   */
  public static <S extends Scope> CsvTableWriterFactory<S> csvTableWriter(
    final Factory<? super S, ? extends Supplier<Table>> tableSupplier,
    final Factory<? super S, ? extends Path> path,
    final boolean append,
    final boolean clearAfterWriting
  ) {
    return new CsvTableWriterFactory<>(
      tableSupplier,
      path,
      append,
      clearAfterWriting
    );
  }

  /**
   * @param tableDefinition factory for the shape of the table to build
   * @param mapSupplier     factory for the supplier of the map to build the
   *                        table from
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TableFromMap}
   * @see TableFromMap
   */
  public static <S extends Scope, K, V> TableFromMapFactory<
    S,
    K,
    V
  > tableFromMap(
    final Factory<? super S, ? extends TableDefinition> tableDefinition,
    final Factory<? super S, ? extends Supplier<Map<K, V>>> mapSupplier
  ) {
    return new TableFromMapFactory<>(tableDefinition, mapSupplier);
  }

  /**
   * @param tableDefinition factory for the shape of the table to grow
   * @param values          one value per column, in column order, each asked
   *                        with no context
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link SteppableTable}
   * @see SteppableTable
   */
  @SafeVarargs
  public static SteppableTableFactory steppableTable(
    final Factory<? super SimulationScope, TableDefinition> tableDefinition,
    final ValueFactory<? super SimulationScope, ? super Void, ?>... values
  ) {
    return new SteppableTableFactory(
      tableDefinition,
      ImmutableList.copyOf(values)
    );
  }

  /**
   * @param columnName the name of the column
   * @param columnType the {@code tablesaw} {@code ColumnType} name (e.g.
   *                   {@code "STRING"}, {@code "DOUBLE"})
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link ColumnDefinition}
   * @see ColumnDefinition
   */
  public static ColumnDefinitionFactory columnDefinition(
    final String columnName,
    final String columnType
  ) {
    return new ColumnDefinitionFactory(columnName, columnType);
  }

  /**
   * @param columnDefinitions factories for the table's columns, in column order
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TableDefinition} over the resolved columns
   * @see TableDefinition
   */
  @SafeVarargs
  public static <
    S extends Scope,
    T extends ColumnDefinition
  > TableDefinitionFactory<S> tableDefinition(
    final Factory<? super S, T>... columnDefinitions
  ) {
    return tableDefinition(
      new ListFactory<S, T>(ImmutableList.copyOf(columnDefinitions))
    );
  }

  /**
   * @param columnDefinitions factory for the list of the table's columns, in
   *                          column order
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TableDefinition} over the resolved columns
   * @see TableDefinition
   */
  public static <S extends Scope> TableDefinitionFactory<S> tableDefinition(
    final Factory<
      ? super S,
      ? extends List<? extends ColumnDefinition>
    > columnDefinitions
  ) {
    return new TableDefinitionFactory<>(columnDefinitions);
  }

  /**
   * @param table        factory for the table to read
   * @param keyBuilder   factory for the function deriving each row's map key
   * @param valueBuilder factory for the function deriving each row's map value
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
   *         immutable map built from the resolved table's rows
   * @see MapFromTableFactory
   */
  public static <S extends Scope, K, V> MapFromTableFactory<
    S,
    K,
    V
  > mapFromTable(
    final Factory<? super S, Table> table,
    final Factory<
      ? super S,
      ? extends Function<? super Row, ? extends K>
    > keyBuilder,
    final Factory<
      ? super S,
      ? extends Function<? super Row, ? extends V>
    > valueBuilder
  ) {
    return new MapFromTableFactory<>(table, keyBuilder, valueBuilder);
  }

  /**
   * @param keyColumnNames the non-empty list of columns whose values are joined
   *                       into the key
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link MultiStringKeyFromRow} over the given columns
   * @see MultiStringKeyFromRow
   */
  public static MultiStringKeyFromRowFactory multiStringKeyFromRow(
    final String... keyColumnNames
  ) {
    return new MultiStringKeyFromRowFactory(
      ImmutableList.copyOf(keyColumnNames)
    );
  }

  /**
   * @param columnName the column to read as a {@code double}
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link DoubleFromRow} over {@code columnName}
   * @see DoubleFromRow
   */
  public static DoubleFromRowFactory doubleFromRow(final String columnName) {
    return new DoubleFromRowFactory(columnName);
  }

  /**
   * @param key             factory for the mapping that reads the lookup key
   *                        from the thing mapped
   * @param table           factory for the table to look values up in
   * @param rowKeyBuilder   factory for the function deriving each table row's
   *                        map key
   * @param rowValueBuilder factory for the function deriving each table row's
   *                        map value
   * @return a {@link MappingFactory} for a
   *         {@link uk.ac.ox.poseidon.core.functions.MapLookup} of the key in
   *         the map built from the resolved table
   * @see uk.ac.ox.poseidon.core.functions.MapLookup
   */
  public static <S extends Scope, C, T, K, V> MapLookupFactory<
    S,
    C,
    T,
    K,
    V
  > tableLookup(
    final MappingFactory<? super S, ? super C, ? super T, ? extends K> key,
    final Factory<? super S, Table> table,
    final Factory<
      ? super S,
      ? extends Function<? super Row, ? extends K>
    > rowKeyBuilder,
    final Factory<
      ? super S,
      ? extends Function<? super Row, ? extends V>
    > rowValueBuilder
  ) {
    return mapLookup(key, mapFromTable(table, rowKeyBuilder, rowValueBuilder));
  }

  /**
   * @param key             factory for the value that is the lookup key
   * @param table           factory for the table to look values up in
   * @param rowKeyBuilder   factory for the function deriving each table row's
   *                        map key
   * @param rowValueBuilder factory for the function deriving each table row's
   *                        map value
   * @return a {@link ValueFactory} for a
   *         {@link uk.ac.ox.poseidon.core.functions.MapLookup} of the key in
   *         the map built from the resolved table, made a value by
   *         {@link uk.ac.ox.poseidon.core.functions.MappingAsValue}
   * @see uk.ac.ox.poseidon.core.functions.MapLookup
   */
  public static <S extends Scope, C, K, V> MappingAsValueFactory<
    S,
    C,
    V
  > tableLookup(
    final ValueFactory<? super S, ? super C, ? extends K> key,
    final Factory<? super S, Table> table,
    final Factory<
      ? super S,
      ? extends Function<? super Row, ? extends K>
    > rowKeyBuilder,
    final Factory<
      ? super S,
      ? extends Function<? super Row, ? extends V>
    > rowValueBuilder
  ) {
    return mapLookup(key, mapFromTable(table, rowKeyBuilder, rowValueBuilder));
  }
}
