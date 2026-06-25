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
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class IndexedBiomassCatchabilityGearTest {

    private static final double EPSILON = 1e-9;
    private static final Supplier<Duration> DURATION = () -> Duration.ofHours(1);

    private static final Species SPECIES_A = new Species("A", null, "Alpha");
    private static final Species SPECIES_B = new Species("B", null, "Beta");
    private static final Species SPECIES_C = new Species("C", null, "Gamma");
    private static final double MINIMUM_CATCH_THRESHOLD = 1.0;

    @Test
    void fish_sameSpeciesIndex_scalesByIndexPosition() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(SPECIES_A, SPECIES_B);
        final IndexedBiomassCatchabilityGear gear =
            new IndexedBiomassCatchabilityGear(
                "G1",
                SpeciesIndexedDoubleArray.of(new double[]{0.2, 0.6}, speciesIndex),
                MINIMUM_CATCH_THRESHOLD,
                DURATION
            );
        final Bucket availableFish =
            Bucket.of(new double[]{10.0, 5.0}, speciesIndex);
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_A)).isCloseTo(2.0, offset(EPSILON));
        assertThat(caught.getKg(SPECIES_B)).isCloseTo(3.0, offset(EPSILON));
    }

    @Test
    void fish_differentSpeciesIndex_ignoresMissingSpecies() {
        final SpeciesIndex gearIndex = SpeciesIndex.of(SPECIES_A, SPECIES_B);
        final IndexedBiomassCatchabilityGear gear =
            new IndexedBiomassCatchabilityGear(
                "G1",
                SpeciesIndexedDoubleArray.of(new double[]{0.1, 0.5}, gearIndex),
                MINIMUM_CATCH_THRESHOLD,
                DURATION
            );
        final Bucket availableFish =
            BiomassBucket.ofBiomassMap(Map.of(SPECIES_B, 8.0, SPECIES_C, 4.0));
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_B)).isCloseTo(4.0, offset(EPSILON));
        assertThat(caught.getKg(SPECIES_C)).isCloseTo(0.0, offset(EPSILON));
    }

    @Test
    void constructor_rejectsOutOfRangeProportions() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(SPECIES_A, SPECIES_B);

        assertThatThrownBy(() -> new IndexedBiomassCatchabilityGear(
            "G1",
            SpeciesIndexedDoubleArray.of(new double[]{0.3, 1.2}, speciesIndex),
            MINIMUM_CATCH_THRESHOLD,
            DURATION
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fish_returnsZero_belowThreshold() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(SPECIES_A);
        final IndexedBiomassCatchabilityGear gear =
            new IndexedBiomassCatchabilityGear(
                "G1",
                SpeciesIndexedDoubleArray.of(new double[]{0.5}, speciesIndex),
                MINIMUM_CATCH_THRESHOLD,
                DURATION
            );
        final Bucket availableFish =
            Bucket.of(new double[]{1.0}, speciesIndex);
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_A)).isCloseTo(0.0, offset(EPSILON));
    }

    @Test
    void constructor_rejectsNegativeThreshold() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(SPECIES_A, SPECIES_B);
        assertThatThrownBy(() -> new IndexedBiomassCatchabilityGear(
            "G1",
            SpeciesIndexedDoubleArray.of(new double[]{0.3, 0.5}, speciesIndex),
            -1.0,
            DURATION
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void constructor_rejectsNullProportions() {
        assertThatThrownBy(() -> new IndexedBiomassCatchabilityGear(
            "G1",
            null,
            MINIMUM_CATCH_THRESHOLD,
            DURATION
        )).isInstanceOf(NullPointerException.class);
    }

    private static final class StubFisheable implements Fisheable {

        private final Bucket availableFish;

        private StubFisheable(final Bucket availableFish) {
            this.availableFish = availableFish;
        }

        @Override
        public Bucket availableFish() {
            return availableFish;
        }

        @Override
        public void release(final Bucket fishToRelease) {
        }

        @Override
        public Bucket extract(final Bucket bucket) {
            return bucket;
        }
    }
}
