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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.io.tables.Factories.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class TableMappingsTest {

  @Test
  void convertsTheCurrentMapToAFreshTypedTableOnEveryRead() {
    final Map<String, Double> entries = new LinkedHashMap<>();
    entries.put("B", 2.5);
    entries.put("A", 1.25);
    final var current = new AtomicReference<>(entries);
    final var supplier = tableFromMap(
      tableDefinition(
        columnDefinition("key", "STRING"),
        columnDefinition("value", "DOUBLE")
      ),
      _ -> current::get
    ).get(GLOBAL_SCOPE);

    final Table first = supplier.get();

    assertThat(first.columnNames()).containsExactly("key", "value");
    assertThat(first.stringColumn("key").asList()).containsExactly("B", "A");
    assertThat(first.doubleColumn("value").asDoubleArray()).containsExactly(
      2.5,
      1.25
    );
    current.set(Map.of("C", 7.5));

    final Table second = supplier.get();

    assertThat(second).isNotSameAs(first);
    assertThat(second.stringColumn("key").asList()).containsExactly("C");
    assertThat(second.doubleColumn("value").asDoubleArray()).containsExactly(
      7.5
    );
    assertThat(first.rowCount()).isEqualTo(2);
    current.set(Map.of());
    assertThat(supplier.get().rowCount()).isZero();
  }

  @Test
  void buildsAnImmutableMapFromTheNamedColumnsOfEveryRow() {
    final var table = tableFromCsvString(
      "gear,port,value\nOTB,A,1.25\nPS,B,2.5\n"
    );

    final Map<String, Double> result = mapFromTable(
      table,
      multiStringKeyFromRow("port", "gear"),
      doubleFromRow("value")
    ).get(GLOBAL_SCOPE);

    assertThat(result).containsExactly(
      Map.entry("A;OTB", 1.25),
      Map.entry("B;PS", 2.5)
    );
    assertThatThrownBy(() -> result.put("extra", 3.0)).isInstanceOf(
      UnsupportedOperationException.class
    );
  }

  @Test
  void anEmptyTableGivesAnEmptyMap() {
    final Table table = Table.create(
      StringColumn.create("key"),
      DoubleColumn.create("value")
    );

    assertThat(
      mapFromTable(
        _ -> table,
        multiStringKeyFromRow("key"),
        doubleFromRow("value")
      ).get(GLOBAL_SCOPE)
    ).isEmpty();
  }

  @Test
  void rejectsDuplicateDerivedKeysInsteadOfLosingARow() {
    final var table = tableFromCsvString("key,value\nA,1.25\nA,2.5\n");

    assertThatThrownBy(() ->
      mapFromTable(
        table,
        multiStringKeyFromRow("key"),
        doubleFromRow("value")
      ).get(GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void extractsCompositeKeysInConfiguredOrderFromTheCurrentRow() {
    final Table table = Table.create(
      StringColumn.create("port", " A ", "B"),
      IntColumn.create("year", 2025, 2026)
    );
    final var key = multiStringKeyFromRow("year", "port").get(GLOBAL_SCOPE);
    final var singleKey = multiStringKeyFromRow("port").get(GLOBAL_SCOPE);

    assertThat(key.apply(table.row(0))).isEqualTo("2025;A");
    assertThat(key.apply(table.row(1))).isEqualTo("2026;B");
    assertThat(singleKey.apply(table.row(1))).isEqualTo("B");
    assertThatThrownBy(() ->
      multiStringKeyFromRow().get(GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void extractsTheNamedDoubleFromTheCurrentRowIncludingMissingValues() {
    final var values = DoubleColumn.create("value", 1.25, -2.5);
    values.appendMissing();
    final Table table = Table.create(values);
    final var extractor = doubleFromRow("value").get(GLOBAL_SCOPE);

    assertThat(extractor.apply(table.row(0))).isEqualTo(1.25);
    assertThat(extractor.apply(table.row(1))).isEqualTo(-2.5);
    assertThat(extractor.apply(table.row(2))).isNaN();
  }

  @Test
  void lookupOverloadsReadLiveKeysAndDistinguishMissingKeysFromZero() {
    final var table = tableFromCsvString("key,value\nA,0.0\nB,2.5\n");
    final var rowKey = multiStringKeyFromRow("key");
    final var rowValue = doubleFromRow("value");
    final ValueFactory<Scope, String, String> valueKey = _ -> context ->
      context;
    final MappingFactory<Scope, String, String, String> mappingKey = _ ->
      (context, item) ->
        context + item;
    final var valueLookup = tableLookup(valueKey, table, rowKey, rowValue).get(
      GLOBAL_SCOPE
    );
    final var valueWithDefault = tableLookup(
      valueKey,
      table,
      rowKey,
      rowValue,
      -1.0
    ).get(GLOBAL_SCOPE);
    final var mappingLookup = tableLookup(
      mappingKey,
      table,
      rowKey,
      rowValue
    ).get(GLOBAL_SCOPE);
    final var mappingWithDefault = tableLookup(
      mappingKey,
      table,
      rowKey,
      rowValue,
      -1.0
    ).get(GLOBAL_SCOPE);

    assertThat(valueLookup.apply("A")).isEqualTo(0.0);
    assertThat(valueLookup.apply("B")).isEqualTo(2.5);
    assertThat(valueWithDefault.apply("A")).isEqualTo(0.0);
    assertThat(valueWithDefault.apply("missing")).isEqualTo(-1.0);
    assertThatThrownBy(() -> valueLookup.apply("missing"))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("missing");
    assertThat(mappingLookup.apply("", "A")).isEqualTo(0.0);
    assertThat(mappingLookup.apply("B", "")).isEqualTo(2.5);
    assertThat(mappingWithDefault.apply("", "A")).isEqualTo(0.0);
    assertThat(mappingWithDefault.apply("mis", "sing")).isEqualTo(-1.0);
    assertThatThrownBy(() -> mappingLookup.apply("mis", "sing"))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("missing");
  }
}
