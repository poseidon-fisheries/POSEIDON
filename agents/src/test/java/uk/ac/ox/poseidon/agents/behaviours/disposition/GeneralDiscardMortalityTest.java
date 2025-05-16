package uk.ac.ox.poseidon.agents.behaviours.disposition;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneralDiscardMortalityTest {

    /**
     * Tests the `partition` method of the `ProportionalDiscardMortality` class. The method is
     * expected to apply a proportional mortality rate (provided by the supplier) to the biomass in
     * the discarded-alive category, moving the corresponding amount to the discarded-dead
     * category.
     */

    @Test
    void partition_appliesMortalityRateCorrectly() {
        // Arrange
        final double mortalityRate = 0.5;
        final DoubleSupplier mortalityRateSupplier = () -> mortalityRate;

        final GeneralDiscardMortality generalDiscardMortality =
            new GeneralDiscardMortality(mortalityRateSupplier);

        final Bucket<Biomass> retained = Bucket.of(DummySpecies.A, Biomass.ofKg(100.0));
        final Bucket<Biomass> discardedAlive = Bucket.of(DummySpecies.A, Biomass.ofKg(50.0));
        final Bucket<Biomass> discardedDead = Bucket.of(DummySpecies.A, Biomass.ofKg(20.0));

        final Disposition<Biomass> currentDisposition = new Disposition<>(
            retained,
            discardedAlive,
            discardedDead
        );

        // Act
        final Disposition<Biomass> result = generalDiscardMortality.partition(
            currentDisposition,
            0.0
        );

        // Assert
        assertEquals(retained, result.getRetained());
        assertEquals(
            Bucket.of(DummySpecies.A, Biomass.ofKg(25.0)),
            result.getDiscardedAlive()
        );
        assertEquals(
            Bucket.of(DummySpecies.A, Biomass.ofKg(45.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_handlesEmptyDiscardedAlive() {
        // Arrange
        final double mortalityRate = 0.3;
        final DoubleSupplier mortalityRateSupplier = () -> mortalityRate;

        final GeneralDiscardMortality generalDiscardMortality =
            new GeneralDiscardMortality(mortalityRateSupplier);

        final Bucket<Biomass> retained = Bucket.of(DummySpecies.B, Biomass.ofKg(100.0));
        final Bucket<Biomass> discardedAlive = Bucket.of(DummySpecies.B, Biomass.ofKg(0.0));
        final Bucket<Biomass> discardedDead = Bucket.of(DummySpecies.B, Biomass.ofKg(20.0));

        final Disposition<Biomass> currentDisposition = new Disposition<>(
            retained,
            discardedAlive,
            discardedDead
        );

        // Act
        final Disposition<Biomass> result = generalDiscardMortality.partition(
            currentDisposition,
            0.0
        );

        // Assert
        assertEquals(retained, result.getRetained());
        assertEquals(
            Bucket.of(DummySpecies.B, Biomass.ofKg(0.0)),
            result.getDiscardedAlive()
        );
        assertEquals(
            Bucket.of(DummySpecies.B, Biomass.ofKg(20.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_handlesZeroMortalityRate() {
        // Arrange
        final double mortalityRate = 0.0;
        final DoubleSupplier mortalityRateSupplier = () -> mortalityRate;

        final GeneralDiscardMortality generalDiscardMortality =
            new GeneralDiscardMortality(mortalityRateSupplier);

        final Bucket<Biomass> retained = Bucket.of(DummySpecies.C, Biomass.ofKg(100.0));
        final Bucket<Biomass> discardedAlive = Bucket.of(DummySpecies.C, Biomass.ofKg(50.0));
        final Bucket<Biomass> discardedDead = Bucket.of(DummySpecies.C, Biomass.ofKg(20.0));

        final Disposition<Biomass> currentDisposition = new Disposition<>(
            retained,
            discardedAlive,
            discardedDead
        );

        // Act
        final Disposition<Biomass> result = generalDiscardMortality.partition(
            currentDisposition,
            0.0
        );

        // Assert
        assertEquals(retained, result.getRetained());
        assertEquals(
            Bucket.of(DummySpecies.C, Biomass.ofKg(50.0)),
            result.getDiscardedAlive()
        );
        assertEquals(Bucket.of(DummySpecies.C, Biomass.ofKg(20.0)), result.getDiscardedDead());
    }

    @Test
    void partition_handlesFullMortalityRate() {
        // Arrange
        final double mortalityRate = 1.0;
        final DoubleSupplier mortalityRateSupplier = () -> mortalityRate;

        final GeneralDiscardMortality generalDiscardMortality =
            new GeneralDiscardMortality(mortalityRateSupplier);

        final Species species = DummySpecies.A;

        final Bucket<Biomass> retained = Bucket.of(species, Biomass.ofKg(100.0));
        final Bucket<Biomass> discardedAlive = Bucket.of(species, Biomass.ofKg(70.0));
        final Bucket<Biomass> discardedDead = Bucket.of(species, Biomass.ofKg(30.0));

        final Disposition<Biomass> currentDisposition = new Disposition<>(
            retained,
            discardedAlive,
            discardedDead
        );

        // Act
        final Disposition<Biomass> result = generalDiscardMortality.partition(
            currentDisposition,
            0.0
        );

        // Assert
        assertEquals(retained, result.getRetained());
        assertEquals(Bucket.of(species, Biomass.ofKg(0.0)), result.getDiscardedAlive());
        assertEquals(
            Bucket.of(species, Biomass.ofKg(100.0)),
            result.getDiscardedDead()
        );
    }
}
