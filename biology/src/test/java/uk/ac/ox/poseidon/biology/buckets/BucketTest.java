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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class BucketTest {

    abstract Bucket newBucket(Map<Species, Double> map);

    private final Species a = new Species("A", null, "A");
    private final Species bA = new Species("B", "adult", "B");
    private final Species bJ = new Species("B", "juvenile", "B");
    private final Species c = new Species("C", null, "C");
    private final Species d = new Species("D", null, "D");

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertContainsBiomass(
        final Optional<? extends Content> maybeContent,
        final double expectedKg
    ) {
        assertThat(maybeContent).hasValueSatisfying(
            content -> assertThat(content).isEqualTo(Biomass.ofKg(expectedKg))
        );
    }

    @Test
    void simpleBucketCreation() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        assertContainsBiomass(bucket.getContent(a), 100.0);
        assertContainsBiomass(bucket.getContent(bA), 200.0);
        assertContainsBiomass(bucket.getContent(bJ), 300.0);
        assertContainsBiomass(bucket.getContent(c), 400.0);
        assertThat(bucket.getContent(d)).isEmpty();
    }

    @Test
    void bucketCreationWithAZeroBiomassSpecies() {
        final Bucket bucket = newBucket(Map.of(a, 0.0, bA, 100.0, bJ, 200.0));
        assertThat(bucket.getContent(a)).isEmpty();
        assertContainsBiomass(bucket.getContent(bA), 100.0);
        assertContainsBiomass(bucket.getContent(bJ), 200.0);
        assertThat(bucket.getKg(a)).isEqualTo(0);
        assertThat(bucket.getKg(bA)).isEqualTo(100.0);
        assertThat(bucket.getKg(bJ)).isEqualTo(200.0);
    }

    @Test
    void simpleAddition() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket bucket2 = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket bucket3 = bucket1.add(bucket2);
        assertContainsBiomass(bucket3.getContent(a), 200.0);
        assertContainsBiomass(bucket3.getContent(bA), 400.0);
        assertContainsBiomass(bucket3.getContent(bJ), 600.0);
        assertContainsBiomass(bucket3.getContent(c), 800.0);
    }

    @Test
    void additionOfBucketsWithDifferentSpecies() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0));
        final Bucket bucket2 = newBucket(Map.of(bA, 200.0, bJ, 300.0));
        final Bucket bucket3 = bucket1.add(bucket2);
        assertContainsBiomass(bucket3.getContent(a), 100.0);
        assertContainsBiomass(bucket3.getContent(bA), 400.0);
        assertContainsBiomass(bucket3.getContent(bJ), 300.0);
    }

    @Test
    void additionIsCommutative() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0));
        final Bucket bucket2 = newBucket(Map.of(bA, 50.0, c, 300.0));
        assertThat(bucket1.add(bucket2).getMap()).isEqualTo(bucket2.add(bucket1).getMap());
    }

    @Test
    void additionIsAssociative() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0));
        final Bucket bucket2 = newBucket(Map.of(bA, 50.0, c, 300.0));
        final Bucket bucket3 = newBucket(Map.of(a, 25.0, d, 75.0));
        assertThat(bucket1.add(bucket2).add(bucket3).getMap())
            .isEqualTo(bucket1.add(bucket2.add(bucket3)).getMap());
    }

    @Test
    void addingEmptyBucketIsNoOp() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0));
        assertThat(bucket.add(Bucket.empty()).getMap()).isEqualTo(bucket.getMap());
    }

    @Test
    void missingSpeciesRemainsEmptyAfterAddition() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0, bA, 200.0));
        final Bucket bucket2 = newBucket(Map.of(bA, 200.0, bJ, 300.0));
        final Bucket bucket3 = bucket1.add(bucket2);
        assertThat(bucket3.getContent(d)).isEmpty();
        assertThat(bucket3.getKg(d)).isEqualTo(0.0);
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
    void subtractingEmptyBucketIsNoOp() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0));
        assertThat(bucket.subtract(Bucket.empty()).getMap()).isEqualTo(bucket.getMap());
    }

    @Test
    void subtractingMoreThanAvailableThrows() {
        final Bucket bucket1 = newBucket(Map.of(a, 100.0));
        final Bucket bucket2 = newBucket(Map.of(a, 200.0));
        assertThatThrownBy(() -> bucket1.subtract(bucket2))
            .isInstanceOfAny(IllegalStateException.class, IllegalArgumentException.class);
    }

    @Test
    void replaceContent() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final Bucket newBucket = bucket.replaceContent(a, Biomass.ofKg(50.0));
        assertContainsBiomass(bucket.getContent(a), 100.0);
        assertContainsBiomass(newBucket.getContent(a), 50.0);
    }

    @Test
    void mapContent() {
        final Bucket bucket = newBucket(Map.of(a, 100.0, bA, 200.0, bJ, 300.0, c, 400.0));
        final var newBucket = bucket.mapContent((species, content) -> content.multiply(2.0));
        assertContainsBiomass(newBucket.getContent(a), 200.0);
        assertContainsBiomass(newBucket.getContent(bA), 400.0);
        assertContainsBiomass(newBucket.getContent(bJ), 600.0);
        assertContainsBiomass(newBucket.getContent(c), 800.0);
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
