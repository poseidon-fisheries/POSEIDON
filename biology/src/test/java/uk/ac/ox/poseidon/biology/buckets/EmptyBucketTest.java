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
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmptyBucketTest {

    private final Species a = new Species("A", null, "A");

    @Test
    void basicQueriesReturnEmptyOrZero() {
        final Bucket empty = EmptyBucket.INSTANCE;
        assertThat(empty.getContent(a)).isEmpty();
        assertThat(empty.getKg(a)).isEqualTo(0.0);
        assertThat(empty.isEmpty()).isTrue();
        assertThat(empty.getTotalBiomass()).isEqualTo(Biomass.ZERO);
        assertThat(empty.getMap()).isEmpty();
        assertThat(empty.getSpecies()).isEmpty();
    }

    @Test
    void addReturnsOtherBucket() {
        final Bucket other = Bucket.of(Map.of(a, Biomass.ofKg(10.0)));
        final Bucket empty = EmptyBucket.INSTANCE;
        assertThat(empty.add(other)).isSameAs(other);
    }

    @Test
    void subtractEmptyFromEmptyIsNoOp() {
        final Bucket empty = EmptyBucket.INSTANCE;
        assertThat(empty.subtract(Bucket.empty())).isSameAs(empty);
    }

    @Test
    void subtractingNonEmptyFromEmptyThrows() {
        final Bucket empty = EmptyBucket.INSTANCE;
        final Bucket other = Bucket.of(Map.of(a, Biomass.ofKg(10.0)));
        assertThatThrownBy(() -> empty.subtract(other))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceContentCreatesNewBucket() {
        final Bucket empty = EmptyBucket.INSTANCE;
        final Bucket replaced = empty.replaceContent(a, Biomass.ofKg(25.0));
        assertThat(replaced.getContent(a)).hasValueSatisfying(
            content -> assertThat(content).isEqualTo(Biomass.ofKg(25.0))
        );
    }

    @Test
    void mapContentIsNoOp() {
        final Bucket empty = EmptyBucket.INSTANCE;
        assertThat(empty.mapContent((species, content) -> content.multiply(2.0))).isSameAs(empty);
    }

    @Test
    void partitionByReturnsEmptyBuckets() {
        final Bucket empty = EmptyBucket.INSTANCE;
        final var partitions = empty.partitionBy((species, content) -> true);
        assertThat(partitions.get(true)).isSameAs(empty);
        assertThat(partitions.get(false)).isSameAs(empty);
    }

    @Test
    void toBuilderBuildsEmptyBucket() {
        final Bucket empty = EmptyBucket.INSTANCE;
        assertThat(empty.toBuilder().build()).isSameAs(empty);
    }
}
