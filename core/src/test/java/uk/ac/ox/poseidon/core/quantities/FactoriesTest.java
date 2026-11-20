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

package uk.ac.ox.poseidon.core.quantities;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.GRAM;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static tech.units.indriya.unit.Units.LITRE;
import static tech.units.indriya.unit.Units.METRE_PER_SECOND;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.quantities.Factories.kilograms;
import static uk.ac.ox.poseidon.core.quantities.Factories.massOf;
import static uk.ac.ox.poseidon.core.quantities.Factories.speedOf;
import static uk.ac.ox.poseidon.core.quantities.Factories.volumeOf;
import static uk.ac.ox.poseidon.core.quantities.Factories.volumetricFlowRateOf;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Volume;
import org.junit.jupiter.api.Test;
import si.uom.quantity.VolumetricFlowRate;

class FactoriesTest {

  @Test
  void massOfADoubleAndUnitKeepsTheValueAndUnit() {
    final Quantity<Mass> mass = massOf(3.5, KILOGRAM).get(GLOBAL_SCOPE);

    assertThat(mass.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(mass.getUnit()).isEqualTo(KILOGRAM);
  }

  @Test
  void massOfAQuantityKeepsTheValueAndUnit() {
    final Quantity<Mass> mass = massOf(getQuantity(3.5, KILOGRAM)).get(
      GLOBAL_SCOPE
    );

    assertThat(mass.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(mass.getUnit()).isEqualTo(KILOGRAM);
  }

  @Test
  void massOfAStringParsesTheValueAndUnit() {
    final Quantity<Mass> mass = massOf("3.5 kg").get(GLOBAL_SCOPE);

    assertThat(mass.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(mass.getUnit()).isEqualTo(KILOGRAM);
  }

  @Test
  void kilogramsOfAFactoryUnwrapsTheResolvedMass() {
    final Double value = kilograms(massOf(3500.0, GRAM)).get(GLOBAL_SCOPE);

    assertThat(value).isEqualTo(3.5);
  }

  @Test
  void kilogramsOfADoubleIsThatManyKilograms() {
    assertThat(kilograms(3.5).get(GLOBAL_SCOPE)).isEqualTo(3.5);
  }

  @Test
  void volumeOfADoubleAndUnitKeepsTheValueAndUnit() {
    final Quantity<Volume> volume = volumeOf(3.5, LITRE).get(GLOBAL_SCOPE);

    assertThat(volume.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(volume.getUnit()).isEqualTo(LITRE);
  }

  @Test
  void volumeOfAQuantityKeepsTheValueAndUnit() {
    final Quantity<Volume> volume = volumeOf(getQuantity(3.5, LITRE)).get(
      GLOBAL_SCOPE
    );

    assertThat(volume.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(volume.getUnit()).isEqualTo(LITRE);
  }

  @Test
  void volumeOfAStringParsesTheValueAndUnit() {
    final Quantity<Volume> volume = volumeOf("3.5 l").get(GLOBAL_SCOPE);

    assertThat(volume.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(volume.getUnit()).isEqualTo(LITRE);
  }

  @Test
  void speedOfAQuantityKeepsTheValueAndUnit() {
    final Quantity<Speed> speed = speedOf(
      getQuantity(3.5, METRE_PER_SECOND)
    ).get(GLOBAL_SCOPE);

    assertThat(speed.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(speed.getUnit()).isEqualTo(METRE_PER_SECOND);
  }

  @Test
  void speedOfADoubleAndUnitKeepsTheValueAndUnit() {
    final Quantity<Speed> speed = speedOf(3.5, METRE_PER_SECOND).get(
      GLOBAL_SCOPE
    );

    assertThat(speed.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(speed.getUnit()).isEqualTo(METRE_PER_SECOND);
  }

  @Test
  void speedOfAStringParsesTheValueAndUnit() {
    final Quantity<Speed> speed = speedOf("3.5 m/s").get(GLOBAL_SCOPE);

    assertThat(speed.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(speed.getUnit()).isEqualTo(METRE_PER_SECOND);
  }

  @Test
  void volumetricFlowRateOfADoubleAndUnitKeepsTheValueAndUnit() {
    final Quantity<VolumetricFlowRate> rate = volumetricFlowRateOf(
      3.5,
      VolumetricFlowRateFactory.LITRE_PER_HOUR
    ).get(GLOBAL_SCOPE);

    assertThat(rate.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(rate.getUnit()).isEqualTo(
      VolumetricFlowRateFactory.LITRE_PER_HOUR
    );
  }

  @Test
  void volumetricFlowRateOfAQuantityKeepsTheValueAndUnit() {
    final Quantity<VolumetricFlowRate> rate = volumetricFlowRateOf(
      getQuantity(3.5, VolumetricFlowRateFactory.LITRE_PER_HOUR)
    ).get(GLOBAL_SCOPE);

    assertThat(rate.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(rate.getUnit()).isEqualTo(
      VolumetricFlowRateFactory.LITRE_PER_HOUR
    );
  }

  @Test
  void volumetricFlowRateOfAStringParsesTheValueAndUnit() {
    final Quantity<VolumetricFlowRate> rate = volumetricFlowRateOf(
      "3.5 l/h"
    ).get(GLOBAL_SCOPE);

    assertThat(rate.getValue().doubleValue()).isEqualTo(3.5);
    assertThat(rate.getUnit()).isEqualTo(
      VolumetricFlowRateFactory.LITRE_PER_HOUR
    );
  }
}
