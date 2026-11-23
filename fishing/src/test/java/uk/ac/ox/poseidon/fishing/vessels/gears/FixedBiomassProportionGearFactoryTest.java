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

package uk.ac.ox.poseidon.fishing.vessels.gears;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.quantities.Factories.massOf;
import static uk.ac.ox.poseidon.core.quantities.Factories.volumetricFlowRateOf;
import static uk.ac.ox.poseidon.core.quantities.VolumetricFlowRateFactory.LITRE_PER_HOUR;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class FixedBiomassProportionGearFactoryTest {

  @Test
  void expectsTheDurationItIsGivenBesideAVaryingOne() {
    final ValueFactory<Scope, Object, Duration> duration = scope -> context ->
      Duration.ofHours(1);
    final var factory = Factories.fixedBiomassProportionGear(
      0.25,
      massOf(2, KILOGRAM),
      duration,
      _ -> Duration.ofHours(2),
      volumetricFlowRateOf(0.0, LITRE_PER_HOUR)
    );

    final var gear = factory.get(Scope.GLOBAL_SCOPE);

    assertThat(gear.getExpectedDuration()).isEqualTo(Duration.ofHours(2));
  }

  @Test
  void expectsItsConstantDuration() {
    final var factory = Factories.fixedBiomassProportionGear(
      0.25,
      massOf(2, KILOGRAM),
      _ -> Duration.ofHours(3),
      volumetricFlowRateOf(0.0, LITRE_PER_HOUR)
    );

    final var gear = factory.get(Scope.GLOBAL_SCOPE);

    assertThat(gear.getExpectedDuration()).isEqualTo(Duration.ofHours(3));
    assertThat(gear.getDurationSupplier().get()).isEqualTo(Duration.ofHours(3));
  }

  @Test
  void resolvesMinimumCatchThreshold() {
    final ValueFactory<Scope, Object, Duration> duration = scope -> context ->
      Duration.ofHours(1);
    final var factory = Factories.fixedBiomassProportionGear(
      0.25,
      massOf(2, KILOGRAM),
      duration,
      _ -> Duration.ofHours(1),
      volumetricFlowRateOf(0.0, LITRE_PER_HOUR)
    );
    final var gear = factory.get(Scope.GLOBAL_SCOPE);
    assertThat(gear.getMinimumCatchThresholdInKg()).isCloseTo(
      2.0,
      offset(1e-9)
    );
  }

  @Test
  void rejectsNegativeThreshold() {
    final ValueFactory<Scope, Object, Duration> duration = scope -> context ->
      Duration.ofHours(1);
    final var factory = Factories.fixedBiomassProportionGear(
      0.25,
      massOf(-1, KILOGRAM),
      duration,
      _ -> Duration.ofHours(1),
      volumetricFlowRateOf(0.0, LITRE_PER_HOUR)
    );
    assertThatThrownBy(() -> factory.get(Scope.GLOBAL_SCOPE)).isInstanceOf(
      IllegalArgumentException.class
    );
  }
}
