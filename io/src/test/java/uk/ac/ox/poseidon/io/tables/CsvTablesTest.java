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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.io.tables.Factories.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.io.sources.DataSource;

class CsvTablesTest {

  @TempDir
  Path directory;

  @Test
  void readsQuotedUtf8DataFromStringFileAndDataSource() throws IOException {
    final String csv =
      "name,value\n\"Sète, port\",1.25\n\"A \"\"quoted\"\" name\",2.5\n";
    final Path file = directory.resolve("input.csv");
    Files.writeString(file, csv);
    final Factory<Scope, DataSource> source = _ -> () -> new StringReader(csv);
    final List<Factory<Scope, Table>> factories = List.of(
      tableFromCsvString(csv),
      tableFromCsvFile(_ -> file),
      tableFromCsv(source)
    );

    for (final var factory : factories) {
      final Table table = factory.get(GLOBAL_SCOPE);

      assertThat(table.columnNames()).containsExactly("name", "value");
      assertThat(table.stringColumn("name").asList()).containsExactly(
        "Sète, port",
        "A \"quoted\" name"
      );
      assertThat(table.doubleColumn("value").asDoubleArray()).containsExactly(
        1.25,
        2.5
      );
    }
  }

  @Test
  void closesTheResolvedReader() {
    final var reader = spy(new StringReader("name,value\nport,1.25\n"));
    final Factory<Scope, DataSource> source = _ -> () -> reader;

    assertThat(tableFromCsv(source).get(GLOBAL_SCOPE).rowCount()).isEqualTo(1);

    verify(reader).close();
  }

  @Test
  void reportsAnIoFailureWhenClosingTheReader() throws IOException {
    final var reader = spy(new StringReader("name,value\nport,1.25\n"));
    final IOException failure = new IOException("cannot close input");
    doThrow(failure).when(reader).close();
    final Factory<Scope, DataSource> source = _ -> () -> reader;

    assertThatThrownBy(() -> tableFromCsv(source).get(GLOBAL_SCOPE))
      .isInstanceOf(RuntimeException.class)
      .hasCause(failure);
  }

  @ParameterizedTest
  @CsvSource({ "true,true", "true,false", "false,true", "false,false" })
  void writesTheCurrentTableWithTheChosenAppendAndClearBehaviour(
    final boolean append,
    final boolean clearAfterWriting
  ) throws IOException {
    final Path file = directory.resolve("output.csv");
    final Table first = Table.create(StringColumn.create("name", "first"));
    final Table second = Table.create(StringColumn.create("name", "second"));
    final var currentTable = new AtomicReference<>(first);
    final var writer = csvTableWriter(
      _ -> currentTable::get,
      _ -> file,
      append,
      clearAfterWriting
    ).get(GLOBAL_SCOPE);

    writer.step(null);

    assertThat(Files.readAllLines(file)).containsExactly("name", "first");
    assertThat(first.rowCount()).isEqualTo(clearAfterWriting ? 0 : 1);
    currentTable.set(second);

    writer.step(null);

    final List<String> expected = append
      ? List.of("name", "first", "second")
      : List.of("name", "second");
    assertThat(Files.readAllLines(file)).containsExactlyElementsOf(expected);
    assertThat(second.rowCount()).isEqualTo(clearAfterWriting ? 0 : 1);
    assertThat(second.columnNames()).containsExactly("name");
  }

  @Test
  void roundTripsQuotedUtf8AndMultilineCells() {
    final Path file = directory.resolve("quoted.csv");
    final Table table = Table.create(
      StringColumn.create("name", "Sète, port", "A \"quote\"\nand newline"),
      DoubleColumn.create("value", 1.25, -2.5)
    );

    csvTableWriter(_ -> () -> table, _ -> file, false, false)
      .get(GLOBAL_SCOPE)
      .step(null);
    final Table restored = tableFromCsvFile(_ -> file).get(GLOBAL_SCOPE);

    assertThat(
      restored.stringColumn("name").asList()
    ).containsExactlyElementsOf(table.stringColumn("name").asList());
    assertThat(restored.doubleColumn("value").asDoubleArray()).containsExactly(
      1.25,
      -2.5
    );
  }

  @Test
  void aFailedWriteReportsTheIoErrorAndKeepsTheRows() {
    final Table table = Table.create(StringColumn.create("name", "unsaved"));
    final var writer = csvTableWriter(
      _ -> () -> table,
      _ -> directory,
      false,
      true
    ).get(GLOBAL_SCOPE);

    assertThatThrownBy(() -> writer.step(null))
      .isInstanceOf(RuntimeException.class)
      .hasCauseInstanceOf(IOException.class);
    assertThat(table.stringColumn("name").asList()).containsExactly("unsaved");
  }
}
