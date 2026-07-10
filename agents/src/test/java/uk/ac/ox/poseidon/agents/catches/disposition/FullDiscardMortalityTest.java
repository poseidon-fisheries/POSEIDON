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
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullDiscardMortalityTest {

    @Test
    void partition_movesAllDiscardedAliveToDiscardedDead() {
        final Bucket retained = Bucket.of(DummySpecies.A, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(DummySpecies.A, Biomass.ofKg(50.0));
        final Bucket discardedDead = Bucket.of(DummySpecies.A, Biomass.ofKg(20.0));

        final Disposition result = new FullDiscardMortality().partition(
            new Disposition(retained, discardedAlive, discardedDead),
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertTrue(result.getDiscardedAlive().isEmpty());
        assertEquals(
            Bucket.of(DummySpecies.A, Biomass.ofKg(70.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_handlesEmptyDiscardedAlive() {
        final Bucket retained = Bucket.of(DummySpecies.A, Biomass.ofKg(100.0));
        final Bucket discardedDead = Bucket.of(DummySpecies.A, Biomass.ofKg(20.0));

        final Disposition result = new FullDiscardMortality().partition(
            new Disposition(retained, Bucket.empty(), discardedDead),
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertTrue(result.getDiscardedAlive().isEmpty());
        assertEquals(discardedDead, result.getDiscardedDead());
    }

    @Test
    void partition_handlesEmptyDiscardedDead() {
        final Bucket retained = Bucket.of(DummySpecies.A, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(DummySpecies.A, Biomass.ofKg(50.0));
        final Bucket discardedDead = Bucket.empty();

        final Disposition result = new FullDiscardMortality().partition(
            new Disposition(retained, discardedAlive, discardedDead),
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertTrue(result.getDiscardedAlive().isEmpty());
        assertEquals(
            Bucket.of(DummySpecies.A, Biomass.ofKg(50.0)),
            result.getDiscardedDead()
        );
    }

    @Test
    void partition_preservesRetained() {
        final Bucket retained = Bucket.of(DummySpecies.A, Biomass.ofKg(100.0));
        final Bucket discardedAlive = Bucket.of(DummySpecies.A, Biomass.ofKg(30.0));

        final Disposition result = new FullDiscardMortality().partition(
            new Disposition(retained, discardedAlive, Bucket.empty()),
            0.0
        );

        assertEquals(retained, result.getRetained());
    }

    @Test
    void partition_handlesMultipleSpecies() {
        final Bucket retained = Bucket.of(Map.of(
            DummySpecies.A, Biomass.ofKg(100.0),
            DummySpecies.B, Biomass.ofKg(200.0)
        ));
        final Bucket discardedAlive = Bucket.of(Map.of(
            DummySpecies.A, Biomass.ofKg(50.0),
            DummySpecies.B, Biomass.ofKg(25.0)
        ));

        final Disposition result = new FullDiscardMortality().partition(
            new Disposition(retained, discardedAlive, Bucket.empty()),
            0.0
        );

        assertEquals(retained, result.getRetained());
        assertTrue(result.getDiscardedAlive().isEmpty());
        assertThat(result.getDiscardedDead().getKg(DummySpecies.A)).isEqualTo(50.0);
        assertThat(result.getDiscardedDead().getKg(DummySpecies.B)).isEqualTo(25.0);
    }

    @Test
    void usingDefaultConstructor_partition() {
        new FullDiscardMortality().partition(
            new Disposition(Bucket.empty(), Bucket.empty(), Bucket.empty()),
            0.0
        );
    }
}
