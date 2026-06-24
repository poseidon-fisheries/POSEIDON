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
