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

package uk.ac.ox.poseidon.agents.catches.disposition;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class ProportionalSpeciesBiomassRetentionTest {

    private static final double EPSILON = 1e-9;

    @Test
    void partition_discardsProportionally_withMatchingIndex() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(Set.of(DummySpecies.A, DummySpecies.B));
        final double[] proportions = new double[speciesIndex.size()];
        proportions[speciesIndex.indexOf(DummySpecies.A)] = 0.2;
        proportions[speciesIndex.indexOf(DummySpecies.B)] = 0.6;
        final SpeciesIndexedDoubleArray proportionsToDiscard =
            new SpeciesIndexedDoubleArray(proportions, speciesIndex);
        final ProportionalSpeciesBiomassRetention retention =
            new ProportionalSpeciesBiomassRetention(proportionsToDiscard);

        final double[] retainedBiomass = new double[speciesIndex.size()];
        retainedBiomass[speciesIndex.indexOf(DummySpecies.A)] = 10.0;
        retainedBiomass[speciesIndex.indexOf(DummySpecies.B)] = 5.0;
        final Bucket retained = BiomassBucket.of(retainedBiomass, speciesIndex);

        final Disposition result = retention.partition(
            new Disposition(retained, Bucket.empty(), Bucket.empty()),
            0.0
        );

        assertThat(result.getDiscardedAlive().getKg(DummySpecies.A))
            .isCloseTo(2.0, offset(EPSILON));
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.B))
            .isCloseTo(3.0, offset(EPSILON));
        assertThat(result.getRetained().getKg(DummySpecies.A))
            .isCloseTo(8.0, offset(EPSILON));
        assertThat(result.getRetained().getKg(DummySpecies.B))
            .isCloseTo(2.0, offset(EPSILON));
        assertThat(result.getDiscardedDead().isEmpty()).isTrue();
    }

    @Test
    void partition_usesDefaultForSpeciesMissingFromIndex() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(Set.of(DummySpecies.A, DummySpecies.B));
        final double[] proportions = new double[speciesIndex.size()];
        proportions[speciesIndex.indexOf(DummySpecies.A)] = 0.5;
        proportions[speciesIndex.indexOf(DummySpecies.B)] = 0.2;
        final SpeciesIndexedDoubleArray proportionsToDiscard =
            new SpeciesIndexedDoubleArray(proportions, speciesIndex);
        final ProportionalSpeciesBiomassRetention retention =
            new ProportionalSpeciesBiomassRetention(proportionsToDiscard);

        final Bucket retained = BiomassBucket.ofBiomassMap(Map.of(
            DummySpecies.A, 10.0,
            DummySpecies.C, 4.0
        ));

        final Disposition result = retention.partition(
            new Disposition(retained, Bucket.empty(), Bucket.empty()),
            0.0
        );

        assertThat(result.getDiscardedAlive().getKg(DummySpecies.A))
            .isCloseTo(5.0, offset(EPSILON));
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.C))
            .isCloseTo(0.0, offset(EPSILON));
        assertThat(result.getRetained().getKg(DummySpecies.A))
            .isCloseTo(5.0, offset(EPSILON));
        assertThat(result.getRetained().getKg(DummySpecies.C))
            .isCloseTo(4.0, offset(EPSILON));
    }

    @Test
    void constructor_rejectsOutOfRangeProportions() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(Set.of(DummySpecies.A));
        final SpeciesIndexedDoubleArray proportionsToDiscard =
            new SpeciesIndexedDoubleArray(new double[]{1.2}, speciesIndex);

        assertThatThrownBy(() -> new ProportionalSpeciesBiomassRetention(proportionsToDiscard))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
