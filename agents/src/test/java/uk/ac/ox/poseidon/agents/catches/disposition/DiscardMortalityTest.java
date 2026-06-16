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

package uk.ac.ox.poseidon.agents.catches.disposition;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscardMortalityTest {

    private static final double EPSILON = 1e-9;

    @Test
    void partition_appliesMortalityRateCorrectly() {
        final double mortalityRate = 0.5;
        final Function<Species, Double> mortalityRateFunction = species -> mortalityRate;

        final DiscardMortality discardMortality =
            new DiscardMortality(mortalityRateFunction);

        final Bucket retained = Bucket.of(DummySpecies.A, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(DummySpecies.A, Biomass.ofKg(50.0));
        final Bucket discardedDead = Bucket.of(DummySpecies.A, Biomass.ofKg(20.0));

        final Disposition currentDisposition = new Disposition(
            retained,
            discardedAlive,
            discardedDead
        );

        final Disposition result = discardMortality.partition(
            currentDisposition,
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertEquals(
            Bucket.of(DummySpecies.A, Biomass.ofKg(25.0)),
            result.getDiscardedAlive()
        );
        assertEquals(
            Bucket.of(DummySpecies.A, Biomass.ofKg(45.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_handlesEmptyDiscardedAlive() {
        final double mortalityRate = 0.3;
        final Function<Species, Double> mortalityRateFunction = species -> mortalityRate;

        final DiscardMortality discardMortality =
            new DiscardMortality(mortalityRateFunction);

        final Bucket retained = Bucket.of(DummySpecies.B, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(DummySpecies.B, Biomass.ofKg(0.0));
        final Bucket discardedDead = Bucket.of(DummySpecies.B, Biomass.ofKg(20.0));

        final Disposition currentDisposition = new Disposition(
            retained,
            discardedAlive,
            discardedDead
        );

        final Disposition result = discardMortality.partition(
            currentDisposition,
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertEquals(
            Bucket.of(DummySpecies.B, Biomass.ofKg(0.0)),
            result.getDiscardedAlive()
        );
        assertEquals(
            Bucket.of(DummySpecies.B, Biomass.ofKg(20.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_handlesZeroMortalityRate() {
        final double mortalityRate = 0.0;
        final Function<Species, Double> mortalityRateFunction = species -> mortalityRate;

        final DiscardMortality discardMortality =
            new DiscardMortality(mortalityRateFunction);

        final Bucket retained = Bucket.of(DummySpecies.C, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(DummySpecies.C, Biomass.ofKg(50.0));
        final Bucket discardedDead = Bucket.of(DummySpecies.C, Biomass.ofKg(20.0));

        final Disposition currentDisposition = new Disposition(
            retained,
            discardedAlive,
            discardedDead
        );

        final Disposition result = discardMortality.partition(
            currentDisposition,
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertEquals(
            Bucket.of(DummySpecies.C, Biomass.ofKg(50.0)),
            result.getDiscardedAlive()
        );
        assertEquals(Bucket.of(DummySpecies.C, Biomass.ofKg(20.0)), result.getDiscardedDead());
    }

    @Test
    void partition_handlesFullMortalityRate() {
        final double mortalityRate = 1.0;
        final Function<Species, Double> mortalityRateFunction = species -> mortalityRate;

        final DiscardMortality discardMortality =
            new DiscardMortality(mortalityRateFunction);

        final Species species = DummySpecies.A;

        final Bucket retained = Bucket.of(species, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(species, Biomass.ofKg(70.0));
        final Bucket discardedDead = Bucket.of(species, Biomass.ofKg(30.0));

        final Disposition currentDisposition = new Disposition(
            retained,
            discardedAlive,
            discardedDead
        );

        final Disposition result = discardMortality.partition(
            currentDisposition,
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertEquals(Bucket.of(species, Biomass.ofKg(0.0)), result.getDiscardedAlive());
        assertEquals(
            Bucket.of(species, Biomass.ofKg(100.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_appliesDifferentRatePerSpecies() {
        final Map<Species, Double> rates = Map.of(
            DummySpecies.A, 0.5,
            DummySpecies.B, 0.1
        );
        final DiscardMortality discardMortality =
            new DiscardMortality(rates::get);

        final Bucket retained = Bucket.empty();
        final Bucket discardedAlive = Bucket.of(Map.of(
            DummySpecies.A, Biomass.ofKg(50.0),
            DummySpecies.B, Biomass.ofKg(100.0)
        ));

        final Disposition result = discardMortality.partition(
            new Disposition(retained, discardedAlive, Bucket.empty()),
            0.0
        );

        assertThat(result.getDiscardedAlive().getKg(DummySpecies.A))
            .isCloseTo(25.0, offset(EPSILON));
        assertThat(result.getDiscardedDead().getKg(DummySpecies.A))
            .isCloseTo(25.0, offset(EPSILON));
        assertThat(result.getDiscardedAlive().getKg(DummySpecies.B))
            .isCloseTo(90.0, offset(EPSILON));
        assertThat(result.getDiscardedDead().getKg(DummySpecies.B))
            .isCloseTo(10.0, offset(EPSILON));
    }
}
