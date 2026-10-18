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

import lombok.NonNull;

/**
 * Factories for {@link org.joda.money.Money} amounts: a fixed amount, or one
 * read from a table row.
 */
public class Factories {

  private Factories() {}

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a fixed
   *         {@link org.joda.money.Money} amount
   * @see MoneyFactory
   */
  public static MoneyFactory money(
    final double amount,
    final @NonNull String currencyUnit
  ) {
    return new MoneyFactory(amount, currencyUnit);
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link MoneyFromRow}
   * @see MoneyFromRowFactory
   */
  public static MoneyFromRowFactory moneyFromRow(
    final String currencyColumnName,
    final String amountColumnName
  ) {
    return new MoneyFromRowFactory(currencyColumnName, amountColumnName);
  }
}
