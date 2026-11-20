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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

final class MoneyFactoryTest {

  @Test
  void buildsAnAmountInTheCurrencyRoundedHalfToEven() {
    final Money money = MoneyFactory.builder()
      .amount(12.345)
      .currencyCode("USD")
      .build()
      .get(GLOBAL_SCOPE);

    assertThat(money).isEqualTo(Money.of(CurrencyUnit.USD, 12.34));
  }

  @Test
  void refusesAMissingCurrency() {
    final MoneyFactory factory = MoneyFactory.builder().amount(1).build();

    assertThatThrownBy(() -> factory.get(GLOBAL_SCOPE))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("currencyCode");
  }
}
