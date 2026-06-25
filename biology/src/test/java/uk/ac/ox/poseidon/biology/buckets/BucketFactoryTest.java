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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BucketFactoryTest {

    private final Species a = new Species("A", null, "A");

    @Test
    void ofSpeciesAndPositiveBiomassReturnsSingleSpeciesBucket() {
        final Bucket bucket = Bucket.of(a, 10.0);
        assertThat(bucket).isInstanceOf(SingleSpeciesBiomassBucket.class);
        assertThat(bucket.getContent(a)).hasValueSatisfying(
            content -> assertThat(content).isEqualTo(Biomass.ofKg(10.0))
        );
    }

    @Test
    void ofSpeciesAndZeroBiomassReturnsEmptyBucket() {
        assertThat(Bucket.of(a, 0.0)).isSameAs(Bucket.empty());
    }

    @Test
    void ofSpeciesAndNaNBiomassReturnsEmptyBucket() {
        assertThat(Bucket.of(a, Double.NaN)).isSameAs(Bucket.empty());
    }

    @Test
    void ofSpeciesAndNegativeBiomassThrows() {
        assertThatThrownBy(() -> Bucket.of(a, -1.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ofSpeciesAndContentFiltersEmptyContent() {
        final Content emptyContent = new Content() {
            @Override public Biomass multiply(double value) { return Biomass.ZERO; }
            @Override public Biomass divide(double value) { return Biomass.ZERO; }
            @Override public boolean isEmpty() { return true; }
            @Override public Biomass asBiomass() { return Biomass.ZERO; }
        };
        assertThat(Bucket.of(a, emptyContent)).isSameAs(Bucket.empty());
    }

    @Test
    void ofSpeciesAndNonEmptyContentReturnsBucket() {
        final Bucket bucket = Bucket.of(a, Biomass.ofKg(5.0));
        assertThat(bucket.getKg(a)).isEqualTo(5.0);
    }
}
