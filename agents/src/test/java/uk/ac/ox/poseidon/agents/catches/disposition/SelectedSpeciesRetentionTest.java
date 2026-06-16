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
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelectedSpeciesRetentionTest {

    @Test
    void partition_keepsSelectedSpeciesAndDiscardsOthers() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(
            DummySpecies.A, DummySpecies.B, DummySpecies.C
        );
        final double[] retainedBiomass = new double[speciesIndex.size()];
        retainedBiomass[speciesIndex.indexOf(DummySpecies.A)] = 10.0;
        retainedBiomass[speciesIndex.indexOf(DummySpecies.B)] = 5.0;
        retainedBiomass[speciesIndex.indexOf(DummySpecies.C)] = 3.0;
        final Bucket retained = BiomassBucket.of(retainedBiomass, speciesIndex);

        final SelectedSpeciesRetention retention =
            new SelectedSpeciesRetention(List.of(DummySpecies.A, DummySpecies.C));

        final Disposition result = retention.partition(
            new Disposition(retained, Bucket.empty(), Bucket.empty()),
            0.0
        );

        assertThat(result.getRetained().getKg(DummySpecies.A)).isEqualTo(10.0);
        assertThat(result.getRetained().getKg(DummySpecies.B)).isEqualTo(0.0);
        assertThat(result.getRetained().getKg(DummySpecies.C)).isEqualTo(3.0);
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.A)).isEqualTo(0.0);
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.B)).isEqualTo(5.0);
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.C)).isEqualTo(0.0);
        assertThat(result.getDiscardedDead().isEmpty()).isTrue();
    }

    @Test
    void partition_preservesExistingDiscardedAlive() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(DummySpecies.A, DummySpecies.B);
        final double[] retainedBiomass = new double[speciesIndex.size()];
        retainedBiomass[speciesIndex.indexOf(DummySpecies.A)] = 10.0;
        retainedBiomass[speciesIndex.indexOf(DummySpecies.B)] = 5.0;
        final Bucket retained = BiomassBucket.of(retainedBiomass, speciesIndex);

        final double[] discardedAliveBiomass = new double[speciesIndex.size()];
        discardedAliveBiomass[speciesIndex.indexOf(DummySpecies.A)] = 2.0;
        final Bucket discardedAlive = BiomassBucket.of(discardedAliveBiomass, speciesIndex);

        final SelectedSpeciesRetention retention =
            new SelectedSpeciesRetention(List.of(DummySpecies.A));

        final Disposition result = retention.partition(
            new Disposition(retained, discardedAlive, Bucket.empty()),
            0.0
        );

        assertThat(result.getRetained().getKg(DummySpecies.A)).isEqualTo(10.0);
        assertThat(result.getRetained().getKg(DummySpecies.B)).isEqualTo(0.0);
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.A)).isEqualTo(2.0);
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.B)).isEqualTo(5.0);
    }

    @Test
    void partition_noSelectedSpeciesDiscardsAll() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(DummySpecies.A, DummySpecies.B);
        final double[] retainedBiomass = new double[speciesIndex.size()];
        retainedBiomass[speciesIndex.indexOf(DummySpecies.A)] = 10.0;
        retainedBiomass[speciesIndex.indexOf(DummySpecies.B)] = 5.0;
        final Bucket retained = BiomassBucket.of(retainedBiomass, speciesIndex);

        final SelectedSpeciesRetention retention =
            new SelectedSpeciesRetention(List.of());

        final Disposition result = retention.partition(
            new Disposition(retained, Bucket.empty(), Bucket.empty()),
            0.0
        );

        assertThat(result.getRetained().isEmpty()).isTrue();
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.A)).isEqualTo(10.0);
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.B)).isEqualTo(5.0);
    }

    @Test
    void partition_selectsAllSpeciesRetainsAll() {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(DummySpecies.A, DummySpecies.B);
        final double[] retainedBiomass = new double[speciesIndex.size()];
        retainedBiomass[speciesIndex.indexOf(DummySpecies.A)] = 10.0;
        retainedBiomass[speciesIndex.indexOf(DummySpecies.B)] = 5.0;
        final Bucket retained = BiomassBucket.of(retainedBiomass, speciesIndex);

        final SelectedSpeciesRetention retention =
            new SelectedSpeciesRetention(List.of(DummySpecies.A, DummySpecies.B));

        final Disposition result = retention.partition(
            new Disposition(retained, Bucket.empty(), Bucket.empty()),
            0.0
        );

        assertThat(result.getRetained().getKg(DummySpecies.A)).isEqualTo(10.0);
        assertThat(result.getRetained().getKg(DummySpecies.B)).isEqualTo(5.0);
        assertThat(result.getDiscardedAlive().isEmpty()).isTrue();
    }
}
