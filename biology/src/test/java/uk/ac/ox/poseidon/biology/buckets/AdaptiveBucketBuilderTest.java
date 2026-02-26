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

package uk.ac.ox.poseidon.biology.buckets;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdaptiveBucketBuilderTest {

    private final Species a = new Species("A", null, "A");
    private final Species b = new Species("B", null, "B");

    @Test
    void newBuilderBuildsSingleSpeciesBucketWhenOnlyOneBiomassSpeciesPresent() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, Biomass.ofKg(10.0))
            .build();

        assertThat(bucket).isInstanceOf(SingleSpeciesBiomassBucket.class);
    }

    @Test
    void ofMapBuildsSingleSpeciesBucketWhenOnlyOneBiomassSpeciesPresent() {
        final Bucket bucket = Bucket.of(Map.of(a, Biomass.ofKg(10.0)));
        assertThat(bucket).isInstanceOf(SingleSpeciesBiomassBucket.class);
    }

    @Test
    void newBuilderBuildsBiomassBucketForMultipleBiomassSpecies() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, Biomass.ofKg(10.0))
            .put(b, Biomass.ofKg(5.0))
            .build();

        assertThat(bucket).isInstanceOf(BiomassBucket.class);
    }

    @Test
    void singleNonEmptyNonBiomassReturnsContentBucket() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, new TestContent(10.0))
            .build();

        assertThat(bucket).isInstanceOf(ContentBucket.class);
    }

    @Test
    void mixedBiomassAndNonBiomassReturnsContentBucket() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, Biomass.ofKg(10.0))
            .put(b, new TestContent(5.0))
            .build();

        assertThat(bucket).isInstanceOf(ContentBucket.class);
    }

    @Test
    void ignoresEmptyEntriesAndStillSpecializesSingleBiomass() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, Biomass.ofKg(10.0))
            .put(b, Biomass.ZERO)
            .build();

        assertThat(bucket).isInstanceOf(SingleSpeciesBiomassBucket.class);
    }

    @Test
    void allEntriesEmptyReturnsEmptyBucket() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, Biomass.ZERO)
            .put(b, new TestContent(0.0))
            .build();

        assertThat(bucket).isSameAs(Bucket.empty());
    }

    @Test
    void singleEntryAfterFilteringIsNonBiomassReturnsContentBucket() {
        final Bucket bucket = Bucket.newBuilder()
            .put(a, Biomass.ZERO)
            .put(b, new TestContent(5.0))
            .build();

        assertThat(bucket).isInstanceOf(ContentBucket.class);
    }

    private static final class TestContent implements Content {
        private final double kg;

        private TestContent(final double kg) {
            this.kg = kg;
        }

        @Override
        public Biomass multiply(final double value) {
            return Biomass.ofKg(kg * value);
        }

        @Override
        public Biomass divide(final double value) {
            return Biomass.ofKg(kg / value);
        }

        @Override
        public boolean isEmpty() {
            return kg == 0.0;
        }

        @Override
        public Biomass asBiomass() {
            return Biomass.ofKg(kg);
        }
    }
}
