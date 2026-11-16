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
import static uk.ac.ox.poseidon.io.tables.Factories.columnDefinition;
import static uk.ac.ox.poseidon.io.tables.Factories.tableDefinition;

import java.util.List;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.ColumnType;

class TableDefinitionTest {

  @Test
  void createsFreshColumnsWithTheConfiguredNameAndType() {
    final var definition = columnDefinition("catch", "DOUBLE").get(
      GLOBAL_SCOPE
    );
    final var first = definition.get();
    first.appendObj(12.5);

    final var second = definition.get();

    assertThat(second).isNotSameAs(first);
    assertThat(second.name()).isEqualTo("catch");
    assertThat(second.type()).isEqualTo(ColumnType.DOUBLE);
    assertThat(second.size()).isZero();
    assertThat(first.size()).isEqualTo(1);
  }

  @Test
  void bothDefinitionOverloadsCreateIndependentTablesInColumnOrder() {
    final var name = columnDefinition("name", "STRING");
    final var value = columnDefinition("value", "DOUBLE");
    final var definitions = List.of(
      tableDefinition(name, value).get(GLOBAL_SCOPE),
      tableDefinition(_ ->
        List.of(name.get(GLOBAL_SCOPE), value.get(GLOBAL_SCOPE))
      ).get(GLOBAL_SCOPE)
    );

    for (final var definition : definitions) {
      final var first = definition.get();
      first.stringColumn("name").append("port");
      first.doubleColumn("value").append(1.25);

      final var second = definition.get();

      assertThat(second).isNotSameAs(first);
      assertThat(second.columnNames()).containsExactly("name", "value");
      assertThat(second.column(0).type()).isEqualTo(ColumnType.STRING);
      assertThat(second.column(1).type()).isEqualTo(ColumnType.DOUBLE);
      assertThat(second.rowCount()).isZero();
      assertThat(first.rowCount()).isEqualTo(1);
    }
  }

  @Test
  void rejectsAnUnknownColumnType() {
    assertThatThrownBy(() ->
      columnDefinition("value", "NOT_A_TYPE").get(GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
