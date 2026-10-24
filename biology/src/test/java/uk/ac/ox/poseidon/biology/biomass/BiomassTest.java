/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import org.junit.jupiter.api.Test;

class BiomassTest {

  /**
   * Complete tests for the `Biomass` class.
   * <p>
   * The Biomass class provides methods to perform arithmetic operations (add,
   * subtract, multiply, divide) on biomass quantities, represent biomass as a
   * Quantity, check if the biomass is empty, and create new instances of
   * Biomass in kilograms.
   */

  @Test
  void testConstructor_WithNumericValueAndUnit() {
    final Biomass biomass = new Biomass(500, GRAM);

    assertThat(biomass.asKg()).isEqualTo(0.5);
  }

  @Test
  void testOfKg_StaticFactoryMethod() {
    final Biomass biomass = Biomass.ofKg(12.5);

    assertThat(biomass.asKg()).isEqualTo(12.5);
  }

  @Test
  void testAdd_WithPositiveValues() {
    final Biomass biomass1 = Biomass.ofKg(10.0);
    final Biomass biomass2 = Biomass.ofKg(5.0);

    final Biomass result = biomass1.add(biomass2);

    assertThat(result.asKg()).isEqualTo(15.0);
  }

  @Test
  void testSubtract_WithPositiveValues() {
    final Biomass biomass1 = Biomass.ofKg(10.0);
    final Biomass biomass2 = Biomass.ofKg(3.0);

    final Biomass result = biomass1.subtract(biomass2);

    assertThat(result.asKg()).isEqualTo(7.0);
  }

  @Test
  void testMultiply_WithPositiveValue() {
    final Biomass biomass = Biomass.ofKg(2.0);

    final Biomass result = biomass.multiply(3.5);

    assertThat(result.asKg()).isEqualTo(7.0);
  }

  @Test
  void testMultiply_WithZero() {
    final Biomass biomass = Biomass.ofKg(5.0);

    final Biomass result = biomass.multiply(0.0);

    assertThat(result.asKg()).isEqualTo(0.0);
  }

  @Test
  void testDivide_WithPositiveValue() {
    final Biomass biomass = Biomass.ofKg(10.0);

    final Biomass result = biomass.divide(2.0);

    assertThat(result.asKg()).isEqualTo(5.0);
  }

  @Test
  void testDivide_ByValueGreaterThanBiomass() {
    final Biomass biomass = Biomass.ofKg(2.0);

    final Biomass result = biomass.divide(4.0);

    assertThat(result.asKg()).isEqualTo(0.5);
  }

  @Test
  void testIsEmpty_WithZeroBiomass() {
    final Biomass biomass = Biomass.ofKg(0.0);

    assertThat(biomass.isEmpty()).isTrue();
  }

  @Test
  void testIsEmpty_WithNonZeroBiomass() {
    final Biomass biomass = Biomass.ofKg(1.0);

    assertThat(biomass.isEmpty()).isFalse();
  }

  @Test
  void testAsBiomass_ReturnsSelf() {
    final Biomass biomass = Biomass.ofKg(5.0);

    final Biomass result = biomass.asBiomass();

    assertThat(result).isEqualTo(biomass);
  }

  @Test
  void testAsQuantity_ReturnsCorrectQuantity() {
    final Biomass biomass = Biomass.ofKg(4.0);

    final Quantity<Mass> quantity = biomass.asQuantity();

    assertThat(quantity.getValue()).isEqualTo(4);
    assertThat(quantity.getUnit()).isEqualTo(KILOGRAM);
  }

  @Test
  void testToString_ReturnsExpectedFormat() {
    final Biomass biomass = Biomass.ofKg(3.5);

    final String result = biomass.toString();

    assertThat(result).isEqualTo("Biomass(3.5 kg)");
  }
}
