/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

abstract class BucketTest {

    abstract Bucket newBucket(Map<Species, Double> map);

    private final Species a = new Species("A", null, "A");
    private final Species bA = new Species("B", "adult", "B");
    private final Species bJ = new Species("B", "juvenile", "B");
    private final Species c = new Species("C", null, "C");
    private final Species d = new Species("D", null, "D");

    @Test
    void simpleBucketCreation() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        assertThat(bucket.getContent(a)).contains(Biomass.ofKg(100.0));
        assertThat(bucket.getContent(bA)).contains(Biomass.ofKg(200.0));
        assertThat(bucket.getContent(bJ)).contains(Biomass.ofKg(300.0));
        assertThat(bucket.getContent(c)).contains(Biomass.ofKg(400.0));
        assertThat(bucket.getContent(d)).isEmpty();
    }

    @Test
    void bucketCreationWithAZeroBiomassSpecies() {
        final Bucket bucket = newBucket(Map.of(a, 0.0, bA, 100.0, bJ, 200.0));
        assertThat(bucket.getContent(a)).isEmpty();
        assertThat(bucket.getContent(bA)).contains(Biomass.ofKg(100.0));
        assertThat(bucket.getContent(bJ)).contains(Biomass.ofKg(200.0));
        assertThat(bucket.getKg(a)).isEqualTo(0);
        assertThat(bucket.getKg(bA)).isEqualTo(100.0);
        assertThat(bucket.getKg(bJ)).isEqualTo(200.0);
    }

    @Test
    void simpleAddition() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket bucket2 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket bucket3 = bucket1.add(bucket2);
        assertThat(bucket3.getContent(a)).contains(Biomass.ofKg(200.0));
        assertThat(bucket3.getContent(bA)).contains(Biomass.ofKg(400.0));
        assertThat(bucket3.getContent(bJ)).contains(Biomass.ofKg(600.0));
        assertThat(bucket3.getContent(c)).contains(Biomass.ofKg(800.0));
    }

    @Test
    void additionOfBucketsWithDifferentSpecies() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0));
        final Bucket bucket2 = newBucket(Map.of(bA, 200.0, bJ, 300.0));
        final Bucket bucket3 = bucket1.add(bucket2);
        assertThat(bucket3.getContent(a)).contains(Biomass.ofKg(100.0));
        assertThat(bucket3.getContent(bA)).contains(Biomass.ofKg(400.0));
        assertThat(bucket3.getContent(bJ)).contains(Biomass.ofKg(300.0));
    }

    @Test
    void subtraction() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket bucket2 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket bucket3 = bucket1.subtract(bucket2);
        assertThat(bucket3.getContent(a)).isEmpty();
        assertThat(bucket3.getContent(bA)).isEmpty();
        assertThat(bucket3.getContent(bJ)).isEmpty();
        assertThat(bucket3.getContent(c)).isEmpty();
        assertThat(bucket3.getTotalBiomass()).isEqualTo(Biomass.ZERO);
    }

    @Test
    void replaceContent() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket newBucket = bucket.replaceContent(a, Biomass.ofKg(50.0));
        assertThat(bucket.getContent(a)).contains(Biomass.ofKg(100.0));
        assertThat(newBucket.getContent(a)).contains(Biomass.ofKg(50.0));
    }

    @Test
    void mapContent() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final var newBucket = bucket.mapContent(content -> content.multiply(2.0));
        assertThat(newBucket.getContent(a)).contains(Biomass.ofKg(200.0));
        assertThat(newBucket.getContent(bA)).contains(Biomass.ofKg(400.0));
        assertThat(newBucket.getContent(bJ)).contains(Biomass.ofKg(600.0));
        assertThat(newBucket.getContent(c)).contains(Biomass.ofKg(800.0));
    }

    @Test
    void partitionBy() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Map<Boolean, Bucket> result =
            bucket.partitionBy((species, content) -> species.getLifeStage() == null);
        assertThat(result.get(true).getSpecies()).isEqualTo(Set.of(a, c));
        assertThat(result.get(false).getSpecies()).isEqualTo(Set.of(bA, bJ));
    }

    @Test
    void isEmpty() {
        final Bucket b1 = newBucket(Map.of());
        assertThat(b1.isEmpty()).isTrue();
        final Bucket b2 = newBucket(Map.of(a, 0.0));
        assertThat(b2.isEmpty()).isTrue();
        final Bucket b3 = newBucket(Map.of(a, 100.0));
        assertThat(b3.isEmpty()).isFalse();
    }

    @Test
    void getTotalBiomass() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        assertThat(bucket.getTotalBiomass()).isEqualTo(Biomass.ofKg(1000.0));
    }

    @Test
    void getMap() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Map<Species, Content> map = bucket.getMap();
        assertThat(map).isEqualTo(
            Map.of(
                a, Biomass.ofKg(100.0),
                bA, Biomass.ofKg(200.0),
                bJ, Biomass.ofKg(300.0),
                c, Biomass.ofKg(400.0)
            )
        );
    }

    @Test
    void getSpecies() {
        final Bucket b1 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        assertThat(b1.getSpecies()).isEqualTo(Set.of(a, bA, bJ, c));
        final Bucket b2 = newBucket(Map.of(a, 100.0, c, 400.0));
        assertThat(b2.getSpecies()).isEqualTo(Set.of(a, c));
    }
}
