/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

abstract class BucketTest {

    abstract Bucket newBucket(Map<Species, Double> map);

    private final Species a = new Species("A", "A", null);
    private final Species bA = new Species("B", "B", "adult");
    private final Species bJ = new Species("B", "B", "juvenile");
    private final Species c = new Species("C", "C", null);
    private final Species d = new Species("D", "D", null);

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
}
