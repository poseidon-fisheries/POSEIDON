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

package uk.ac.ox.poseidon.fishing.money;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

final class MoneyFromRowTest {

  @Test
  void readsTheCurrencyAndAmountFromTheNamedColumns() {
    final Table table = Table.create(
      StringColumn.create("unit", "EUR", "USD"),
      DoubleColumn.create("value", 2.5, 4.0)
    );

    final MoneyFromRow money = new MoneyFromRow("unit", "value");

    assertThat(money.apply(table.row(0))).isEqualTo(
      Money.of(CurrencyUnit.EUR, 2.5)
    );
    assertThat(money.apply(table.row(1))).isEqualTo(
      Money.of(CurrencyUnit.USD, 4.0)
    );
  }
}
