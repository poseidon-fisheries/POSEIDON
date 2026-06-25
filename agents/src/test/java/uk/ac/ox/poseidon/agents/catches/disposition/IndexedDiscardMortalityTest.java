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
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import static org.assertj.core.api.Assertions.assertThat;

class IndexedDiscardMortalityTest {

    @Test
    void appliesMortalityRatesToDiscardedAlive() {
        final Species a = new Species("A", null, null);
        final Species b = new Species("B", null, null);
        final SpeciesIndex index = SpeciesIndex.of(a, b);
        final Bucket discardedAlive = Bucket.of(new double[]{10.0, 20.0}, index);
        final SpeciesIndexedDoubleArray rates =
            SpeciesIndexedDoubleArray.of(new double[]{0.1, 0.0}, index);
        final Disposition disposition =
            new Disposition(Bucket.empty(), discardedAlive, Bucket.empty());

        final IndexedDiscardMortality process =
            new IndexedDiscardMortality(rates);
        final Disposition updated = process.partition(disposition, 0.0);

        assertThat(updated.getDiscardedAlive().getKg(a)).isEqualTo(9.0);
        assertThat(updated.getDiscardedAlive().getKg(b)).isEqualTo(20.0);
        assertThat(updated.getDiscardedDead().getKg(a)).isEqualTo(1.0);
        assertThat(updated.getDiscardedDead().getKg(b)).isEqualTo(0.0);
    }
}
