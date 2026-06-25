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
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BiomassBucketTest extends BucketTest {

    private final Species a = new Species("A", null, "A");

    @Override
    Bucket newBucket(final Map<Species, Double> map) {
        return BiomassBucket.ofBiomassMap(map);
    }

    @Test
    void mapBiomassValueReturningNegativeThrows() {
        final Bucket bucket = newBucket(Map.of(a, 100.0));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> bucket.mapBiomassValue((species, value) -> -1.0));
    }

    @Test
    void mapBiomassValueReturningNegativeOnOneSpeciesThrows() {
        final Species b = new Species("B", null, "B");
        final Bucket bucket = newBucket(Map.of(a, 100.0, b, 200.0));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> bucket.mapBiomassValue((species, value) ->
                species.equals(b) ? -1.0 : value));
    }

    @Test
    void ofArrayTreatsNaNAsAbsent() {
        final var speciesIndex = SpeciesIndex.of(Set.of(a));
        final Bucket bucket = Bucket.of(new double[]{Double.NaN}, speciesIndex);
        assertThat(bucket).isSameAs(Bucket.empty());
    }

    @Test
    void denseMapWithIndexReturningNaNNormalizesToAbsent() {
        final var speciesIndex = SpeciesIndex.of(Set.of(a));
        final BiomassBucket bucket = BiomassBucket.create(new double[]{100.0}, speciesIndex);
        final BiomassBucket mapped = bucket.mapWithIndex((value, index) -> Double.NaN);
        assertThat(mapped.isEmpty()).isTrue();
        assertThat(mapped.getDouble(0)).isEqualTo(0.0);
    }

    @Test
    void denseMapWithIndexReturningNegativeThrows() {
        final var speciesIndex = SpeciesIndex.of(Set.of(a));
        final BiomassBucket bucket = BiomassBucket.create(new double[]{100.0}, speciesIndex);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> bucket.mapWithIndex((value, index) -> -1.0));
    }

    @Test
    void ofContentMapCreatesBiomassBucket() {
        final Species b = new Species("B", null, "B");
        final Map<Species, Content> map = Map.of(
            a, Biomass.ofKg(10.0),
            b, Biomass.ofKg(20.0)
        );

        final BiomassBucket bucket = BiomassBucket.ofContentMap(map);

        assertThat(bucket.getKg(a)).isEqualTo(10.0);
        assertThat(bucket.getKg(b)).isEqualTo(20.0);
        assertThat(bucket.getSpecies()).isEqualTo(Set.of(a, b));
    }

    @Test
    void addContentBucketMergesCorrectly() {
        final Species b = new Species("B", null, "B");
        final BiomassBucket biomassBucket = (BiomassBucket) newBucket(Map.of(a, 10.0));
        final ContentBucket contentBucket = ContentBucket.ofContentMap(
            Map.of(a, Biomass.ofKg(5.0), b, Biomass.ofKg(3.0))
        );

        final Bucket result = biomassBucket.add(contentBucket);

        assertThat(result.getKg(a)).isEqualTo(15.0);
        assertThat(result.getKg(b)).isEqualTo(3.0);
    }

    @Test
    void subtractContentBucketSubtractsCorrectly() {
        final Species b = new Species("B", null, "B");
        final BiomassBucket biomassBucket = (BiomassBucket) newBucket(Map.of(a, 10.0, b, 5.0));
        final ContentBucket contentBucket = ContentBucket.ofContentMap(
            Map.of(a, Biomass.ofKg(3.0))
        );

        final Bucket result = biomassBucket.subtract(contentBucket);

        assertThat(result.getKg(a)).isEqualTo(7.0);
        assertThat(result.getKg(b)).isEqualTo(5.0);
    }

    @Test
    void replaceContentWithNonBiomassFallsToDefault() {
        final Bucket bucket = newBucket(Map.of(a, 10.0));
        final Content nonBiomass = new NonBiomassContent(6.0);

        final Bucket replaced = bucket.replaceContent(a, nonBiomass);

        assertThat(replaced).isInstanceOf(ContentBucket.class);
        assertThat(replaced.getKg(a)).isEqualTo(6.0);
    }

    @Test
    void ofBiomassMapWithNaNNormalizesToAbsent() {
        final Bucket bucket = BiomassBucket.ofBiomassMap(Map.of(a, Double.NaN));

        assertThat(bucket.isEmpty()).isTrue();
        assertThat(bucket.getContent(a)).isEmpty();
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
