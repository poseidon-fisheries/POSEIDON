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

package uk.ac.ox.poseidon.fishing.market;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

final class PriceTest {

  @Test
  void convertsAPricePerGramToAPricePerKilogram() {
    final Price price = new Price(Money.of(CurrencyUnit.EUR, 2.0), GRAM);

    assertThat(price.valueForKgDouble(1.0)).isEqualTo(2000.0);
    assertThat(
      price.valueFor(Biomass.ofKg(0.5)).getAmount()
    ).isEqualByComparingTo("1000.00");
  }

  @Test
  void keepsAPricePerKilogramAsIs() {
    final Price price = new Price(Money.of(CurrencyUnit.EUR, 2.0), KILOGRAM);

    assertThat(price.valueForKgDouble(3.0)).isEqualTo(6.0);
  }
}
