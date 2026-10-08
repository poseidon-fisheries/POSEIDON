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
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class FixedBiomassProportionGearTest {

    private static final double EPSILON = 1e-9;
    private static final Supplier<Duration> DURATION = () -> Duration.ofHours(1);
    private static final Species SPECIES_A = new Species("A", null, "Alpha");

    @Test
    void fish_appliesProportion_aboveThreshold() {
        final var gear = new FixedBiomassProportionGear("G1", 0.25, 1.0, DURATION, 0.0);
        final Bucket availableFish =
            Bucket.of(SPECIES_A, 10.0);
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_A)).isCloseTo(2.5, offset(EPSILON));
    }

    @Test
    void fish_returnsZero_belowThreshold() {
        final var gear = new FixedBiomassProportionGear("G1", 0.25, 1.0, DURATION, 0.0);
        final Bucket availableFish =
            Bucket.of(SPECIES_A, 3.0);
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_A)).isCloseTo(0.0, offset(EPSILON));
    }

    @Test
    void fish_returnsPositive_atThreshold() {
        final var gear = new FixedBiomassProportionGear("G1", 0.25, 1.0, DURATION, 0.0);
        final Bucket availableFish =
            Bucket.of(SPECIES_A, 4.0);
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_A)).isCloseTo(1.0, offset(EPSILON));
    }

    @Test
    void fish_zeroProportion_returnsZero() {
        final var gear = new FixedBiomassProportionGear("G1", 0.0, 1.0, DURATION, 0.0);
        final Bucket availableFish =
            Bucket.of(SPECIES_A, 100.0);
        final Fisheable fisheable = new StubFisheable(availableFish);

        final Bucket caught = gear.fish(fisheable);

        assertThat(caught.getKg(SPECIES_A)).isCloseTo(0.0, offset(EPSILON));
    }

    @Test
    void constructor_rejectsNegativeThreshold() {
        assertThatThrownBy(() -> new FixedBiomassProportionGear(
            "G1", 0.25, -1.0, DURATION, 0.0
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_rejectsOutOfRangeProportion() {
        assertThatThrownBy(() -> new FixedBiomassProportionGear(
            "G1", 1.5, 1.0, DURATION, 0.0
        )).isInstanceOf(IllegalArgumentException.class);
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
