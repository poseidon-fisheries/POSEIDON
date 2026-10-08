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

package uk.ac.ox.poseidon.agents.vessels.gears;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.mock;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.quantities.Factories.massOf;
import static uk.ac.ox.poseidon.core.quantities.Factories.volumetricFlowRateOf;
import static uk.ac.ox.poseidon.core.quantities.VolumetricFlowRateFactory.LITRE_PER_HOUR;

class FixedBiomassProportionGearFactoryTest {

    @Test
    void resolvesMinimumCatchThreshold() {
        final Factory<SimulationScope, Supplier<Duration>> durationSupplier =
            scope -> () -> Duration.ofHours(1);
        final var factory = Factories.fixedBiomassProportionGear(
            "G1",
            0.25,
            massOf(2, KILOGRAM),
            durationSupplier,
            volumetricFlowRateOf(0.0, LITRE_PER_HOUR)
        );
        final var gear = factory.get(mock(SimulationScope.class));
        assertThat(gear.getCode()).isEqualTo("G1");
        assertThat(gear.getMinimumCatchThresholdInKg()).isCloseTo(2.0, offset(1e-9));
    }

    @Test
    void rejectsNegativeThreshold() {
        final Factory<SimulationScope, Supplier<Duration>> durationSupplier =
            scope -> () -> Duration.ofHours(1);
        final var factory = Factories.fixedBiomassProportionGear(
            "G1",
            0.25,
            massOf(-1, KILOGRAM),
            durationSupplier,
            volumetricFlowRateOf(0.0, LITRE_PER_HOUR)
        );
        assertThatThrownBy(() -> factory.get(mock(SimulationScope.class)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
