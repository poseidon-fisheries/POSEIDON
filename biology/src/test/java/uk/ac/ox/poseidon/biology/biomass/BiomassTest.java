/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.biology.biomass;

import org.junit.jupiter.api.Test;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static javax.measure.MetricPrefix.KILO;
import static org.junit.jupiter.api.Assertions.*;
import static tech.units.indriya.unit.Units.GRAM;

class BiomassTest {

    /**
     * Complete tests for the `Biomass` class.
     * <p>
     * The Biomass class provides methods to perform arithmetic operations (add, subtract, multiply,
     * divide) on biomass quantities, represent biomass as a Quantity, check if the biomass is
     * empty, and create new instances of Biomass in kilograms.
     */

    @Test
    void testConstructor_WithNumericValueAndUnit() {
        // Arrange & Act
        final Biomass biomass = new Biomass(500, GRAM);

        // Assert
        assertEquals(0.5, biomass.getBiomassInKg());
    }

    @Test
    void testOfKg_StaticFactoryMethod() {
        // Arrange & Act
        final Biomass biomass = Biomass.ofKg(12.5);

        // Assert
        assertEquals(12.5, biomass.getBiomassInKg());
    }

    @Test
    void testAdd_WithPositiveValues() {
        // Arrange
        final Biomass biomass1 = Biomass.ofKg(10.0);
        final Biomass biomass2 = Biomass.ofKg(5.0);

        // Act
        final Biomass result = biomass1.add(biomass2);

        // Assert
        assertEquals(15.0, result.getBiomassInKg());
    }

    @Test
    void testSubtract_WithPositiveValues() {
        // Arrange
        final Biomass biomass1 = Biomass.ofKg(10.0);
        final Biomass biomass2 = Biomass.ofKg(3.0);

        // Act
        final Biomass result = biomass1.subtract(biomass2);

        // Assert
        assertEquals(7.0, result.getBiomassInKg());
    }

    @Test
    void testMultiply_WithPositiveValue() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(2.0);

        // Act
        final Biomass result = biomass.multiply(3.5);

        // Assert
        assertEquals(7.0, result.getBiomassInKg());
    }

    @Test
    void testMultiply_WithZero() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(5.0);

        // Act
        final Biomass result = biomass.multiply(0.0);

        // Assert
        assertEquals(0.0, result.getBiomassInKg());
    }

    @Test
    void testDivide_WithPositiveValue() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(10.0);

        // Act
        final Biomass result = biomass.divide(2.0);

        // Assert
        assertEquals(5.0, result.getBiomassInKg());
    }

    @Test
    void testDivide_ByValueGreaterThanBiomass() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(2.0);

        // Act
        final Biomass result = biomass.divide(4.0);

        // Assert
        assertEquals(0.5, result.getBiomassInKg());
    }

    @Test
    void testIsEmpty_WithZeroBiomass() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(0.0);

        // Act & Assert
        assertTrue(biomass.isEmpty());
    }

    @Test
    void testIsEmpty_WithNonZeroBiomass() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(1.0);

        // Act & Assert
        assertFalse(biomass.isEmpty());
    }

    @Test
    void testAsBiomass_ReturnsSelf() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(5.0);

        // Act
        final Biomass result = biomass.asBiomass();

        // Assert
        assertEquals(biomass, result);
    }

    @Test
    void testAsQuantity_ReturnsCorrectQuantity() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(4.0);

        // Act
        final Quantity<Mass> quantity = biomass.asQuantity();

        // Assert
        assertEquals(4, quantity.getValue());
        assertEquals(KILO(GRAM), quantity.getUnit());
    }

    @Test
    void testToString_ReturnsExpectedFormat() {
        // Arrange
        final Biomass biomass = Biomass.ofKg(3.5);

        // Act
        final String result = biomass.toString();

        // Assert
        assertEquals("Biomass(3.5 kg)", result);
    }
}
