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

package uk.ac.ox.poseidon.agents.catches;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CategorisedCatchTest {

    private final Species a = new Species("A", null, "A");
    private final CatchCategory c1 = new CatchCategory("c1");
    private final CatchCategory c2 = new CatchCategory("c2");

    @Test
    void addWithEmptyLeftReturnsOtherInstance() {
        final CategorisedCatch empty = CategorisedCatch.empty();
        final CategorisedCatch other = new CategorisedCatch(
            Map.of(c1, Bucket.of(a, Biomass.ofKg(10.0)))
        );
        assertThat(empty.add(other)).isSameAs(other);
    }

    @Test
    void addWithEmptyRightReturnsThisInstance() {
        final CategorisedCatch left = new CategorisedCatch(
            Map.of(c1, Bucket.of(a, Biomass.ofKg(10.0)))
        );
        assertThat(left.add(CategorisedCatch.empty())).isSameAs(left);
    }

    @Test
    void addMergesBucketsByCategory() {
        final CategorisedCatch left = new CategorisedCatch(
            Map.of(c1, Bucket.of(a, Biomass.ofKg(10.0)))
        );
        final CategorisedCatch right = new CategorisedCatch(
            Map.of(
                c1, Bucket.of(a, Biomass.ofKg(2.0)),
                c2, Bucket.of(a, Biomass.ofKg(3.0))
            )
        );

        final CategorisedCatch merged = left.add(right);
        assertThat(merged.getBuckets().get(c1).getTotalBiomassInKg()).isEqualTo(12.0);
        assertThat(merged.getBuckets().get(c2).getTotalBiomassInKg()).isEqualTo(3.0);
    }

    @Test
    void isEmptyAndTotalBiomassReflectUnderlyingBuckets() {
        final CategorisedCatch empty = new CategorisedCatch(
            Map.of(c1, Bucket.of(a, Biomass.ZERO))
        );
        assertThat(empty.isEmpty()).isTrue();
        assertThat(empty.getTotalBiomassInKg()).isEqualTo(0.0);

        final CategorisedCatch nonEmpty = new CategorisedCatch(
            Map.of(
                c1, Bucket.of(a, Biomass.ofKg(1.5)),
                c2, Bucket.of(a, Biomass.ofKg(2.5))
            )
        );
        assertThat(nonEmpty.isEmpty()).isFalse();
        assertThat(nonEmpty.getTotalBiomassInKg()).isEqualTo(4.0);
    }
}
