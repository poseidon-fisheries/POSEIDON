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

package uk.ac.ox.poseidon.biology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

final class ContentTest {

  private final Content content = mock(Content.class, CALLS_REAL_METHODS);

  @BeforeEach
  void setUpBiomass() {
    when(content.asBiomass()).thenReturn(Biomass.ofKg(2.5));
  }

  @Test
  void convertsContentToTheRequestedMassUnit() {
    assertThat(content.as(GRAM)).isEqualTo(2500);
  }

  @Test
  void exposesContentAsAQuantityInKilograms() {
    final Quantity<Mass> quantity = content.asQuantity();

    assertThat(quantity.getValue().doubleValue()).isEqualTo(2.5);
    assertThat(quantity.getUnit()).isEqualTo(KILOGRAM);
  }

  @Test
  void subtractsTheOtherContentsBiomass() {
    final Content result = content.subtract(Biomass.ofKg(1));

    assertThat(result.asKg()).isEqualTo(1.5);
  }
}
