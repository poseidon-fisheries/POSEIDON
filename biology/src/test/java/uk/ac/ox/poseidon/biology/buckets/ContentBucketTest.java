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

class ContentBucketTest extends BucketTest {

    private final Species speciesA = new Species("A", null, "A");
    private final Species speciesB = new Species("B", null, "B");

    @Override
    Bucket newBucket(final Map<Species, Double> map) {
        return ContentBucket.ofBiomassMap(map);
    }

    @Test
    void addReturnsBiomassBucketWhenAllBiomass() {
        final Bucket bucketA = newBucket(Map.of(speciesA, 3.0));
        final Bucket bucketB = newBucket(Map.of(speciesB, 5.0));

        final Bucket result = bucketA.add(bucketB);

        assertThat(result).isInstanceOf(BiomassBucket.class);
        assertThat(result.getKg(speciesA)).isEqualTo(3.0);
        assertThat(result.getKg(speciesB)).isEqualTo(5.0);
    }

    @Test
    void addReturnsContentBucketWhenMixedContent() {
        final Content nonBiomass = new TestContent(3.0);
        final Bucket bucketA = ContentBucket.ofContentMap(Map.of(speciesA, nonBiomass));
        final Bucket bucketB = newBucket(Map.of(speciesA, 7.0));

        final Bucket result = bucketA.add(bucketB);

        assertThat(result).isInstanceOf(ContentBucket.class);
        assertThat(result.getKg(speciesA)).isEqualTo(10.0);
    }

    @Test
    void addReturnsEmptyBucketWhenAllEntriesEmpty() {
        final Bucket bucketA = newBucket(Map.of(speciesA, 0.0));
        final Bucket bucketB = newBucket(Map.of(speciesA, 0.0));

        final Bucket result = bucketA.add(bucketB);

        assertThat(result).isSameAs(Bucket.empty());
    }

    @Test
    void addWithEmptyBucketReturnsSameBucket() {
        final Bucket bucket = newBucket(Map.of(speciesA, 3.0));

        final Bucket result = bucket.add(Bucket.empty());

        assertThat(result).isInstanceOf(BiomassBucket.class);
        assertThat(result.getKg(speciesA)).isEqualTo(3.0);
    }

    @Test
    void getTotalBiomassWithNonBiomassContent() {
        final Content nonBiomass = new TestContent(10.0);
        final Bucket bucket = ContentBucket.ofContentMap(Map.of(speciesA, nonBiomass));

        assertThat(bucket.getTotalBiomass()).isEqualTo(Biomass.ofKg(10.0));
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

        @Override
        public Content add(final Content content) {
            return new TestContent(kg + content.asKg());
        }
    }
}
