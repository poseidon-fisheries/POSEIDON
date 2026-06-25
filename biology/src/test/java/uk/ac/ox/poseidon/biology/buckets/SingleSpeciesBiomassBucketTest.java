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

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SingleSpeciesBiomassBucketTest {

    private final Species a = new Species("A", null, "A");
    private final Species b = new Species("B", null, "B");

    private static void assertContainsBiomass(
        final Optional<? extends Content> maybeContent,
        final double expectedKg
    ) {
        assertThat(maybeContent).hasValueSatisfying(
            content -> assertThat(content).isEqualTo(Biomass.ofKg(expectedKg))
        );
    }

    @Test
    void constructorRejectsNullSpecies() {
        assertThatThrownBy(() -> new SingleSpeciesBiomassBucket(null, 1.0))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorRejectsZeroBiomass() {
        assertThatThrownBy(() -> new SingleSpeciesBiomassBucket(a, 0.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorRejectsNegativeBiomass() {
        assertThatThrownBy(() -> new SingleSpeciesBiomassBucket(a, -1.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void basicQueriesReturnExpectedValues() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        assertContainsBiomass(bucket.getContent(a), 10.0);
        assertThat(bucket.getContent(b)).isEmpty();
        assertThat(bucket.getKg(a)).isEqualTo(10.0);
        assertThat(bucket.getKg(b)).isEqualTo(0.0);
        assertThat(bucket.getTotalBiomass()).isEqualTo(Biomass.ofKg(10.0));
        assertThat(bucket.getTotalBiomassInKg()).isEqualTo(10.0);
        assertThat(bucket.getSpecies()).isEqualTo(Set.of(a));
        assertThat(bucket.getMap()).isEqualTo(java.util.Map.of(a, Biomass.ofKg(10.0)));
    }

    @Test
    void addSameSpeciesAddsBiomass() {
        final Bucket b1 = new SingleSpeciesBiomassBucket(a, 7.0);
        final Bucket b2 = new SingleSpeciesBiomassBucket(a, 3.0);
        final Bucket result = b1.add(b2);
        assertContainsBiomass(result.getContent(a), 10.0);
        assertThat(result.getContent(b)).isEmpty();
    }

    @Test
    void subtractSameSpeciesSubtractsBiomass() {
        final Bucket b1 = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket b2 = new SingleSpeciesBiomassBucket(a, 3.0);
        final Bucket result = b1.subtract(b2);
        assertContainsBiomass(result.getContent(a), 7.0);
    }

    @Test
    void subtractingSameAmountReturnsEmptyBucket() {
        final Bucket b1 = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket b2 = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket result = b1.subtract(b2);
        assertThat(result).isSameAs(Bucket.empty());
    }

    @Test
    void subtractingMoreThanAvailableThrows() {
        final Bucket b1 = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket b2 = new SingleSpeciesBiomassBucket(a, 11.0);
        assertThatThrownBy(() -> b1.subtract(b2))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceContentForSameSpeciesUpdatesValue() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket replaced = bucket.replaceContent(a, Biomass.ofKg(6.0));
        assertContainsBiomass(replaced.getContent(a), 6.0);
    }

    @Test
    void replacingWithZeroBiomassReturnsEmptyBucket() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket replaced = bucket.replaceContent(a, Biomass.ofKg(0.0));
        assertThat(replaced).isSameAs(Bucket.empty());
    }

    @Test
    void mapBiomassValueCanReturnEmptyBucket() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket mapped = bucket.mapBiomassValue((species, value) -> 0.0);
        assertThat(mapped).isSameAs(Bucket.empty());
    }

    @Test
    void mapBiomassValueReturningNaNReturnsEmptyBucket() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket mapped = bucket.mapBiomassValue((species, value) -> Double.NaN);
        assertThat(mapped).isSameAs(Bucket.empty());
    }

    @Test
    void addDifferentSpeciesReturnsMultiSpeciesBucket() {
        final Bucket bucketA = new SingleSpeciesBiomassBucket(a, 7.0);
        final Bucket bucketB = new SingleSpeciesBiomassBucket(b, 3.0);
        final Bucket result = bucketA.add(bucketB);
        assertThat(result.getKg(a)).isEqualTo(7.0);
        assertThat(result.getKg(b)).isEqualTo(3.0);
        assertThat(result.getSpecies()).isEqualTo(Set.of(a, b));
    }

    @Test
    void subtractDifferentSpeciesReturnsOriginalMinusNothing() {
        final Bucket bucketA = new SingleSpeciesBiomassBucket(a, 7.0);
        final Bucket bucketB = new SingleSpeciesBiomassBucket(b, 3.0);
        final Bucket result = bucketA.subtract(bucketB);
        assertThat(result.getKg(a)).isEqualTo(7.0);
        assertThat(result.getKg(b)).isEqualTo(0.0);
    }

    @Test
    void mapBiomassValueReturningNegativeThrows() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        assertThatThrownBy(() -> bucket.mapBiomassValue((species, value) -> -1.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceContentWithNonBiomassFallsToDefault() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final Content nonBiomass = new NonBiomassContent(6.0);
        final Bucket replaced = bucket.replaceContent(a, nonBiomass);
        assertThat(replaced.getKg(a)).isEqualTo(6.0);
    }

    @Test
    void mapContentReturningNonBiomassFallsToDefault() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final Bucket mapped = bucket.mapContent((species, content) -> new NonBiomassContent(5.0));
        assertThat(mapped.getKg(a)).isEqualTo(5.0);
    }

    @Test
    void partitionBySeparatesSingleEntry() {
        final Bucket bucket = new SingleSpeciesBiomassBucket(a, 10.0);
        final var partitions = bucket.partitionBy((species, content) -> species.equals(a));
        assertThat(partitions.get(true)).isSameAs(bucket);
        assertThat(partitions.get(false)).isSameAs(Bucket.empty());
    }

    private static final class NonBiomassContent implements Content {
        private final double kg;

        private NonBiomassContent(final double kg) {
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
