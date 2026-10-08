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

package uk.ac.ox.poseidon.agents.choices;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

class ExponentialMovingAverageOfBucketsTest {

    private final Species a = new Species("A", null, "A");
    private final Species b = new Species("B", null, "B");
    private final ExponentialMovingAverageOfBuckets rule =
        new ExponentialMovingAverageOfBuckets(0.25);

    @Test
    void speciesInBothBucketsIsAveraged() {
        final Bucket result = rule.apply(Bucket.of(a, 10), Bucket.of(a, 4));

        assertThat(result.getKg(a)).isCloseTo(10 * 0.75 + 4 * 0.25, within(1e-12));
    }

    @Test
    void speciesOnlyRememberedDecays() {
        final Bucket result = rule.apply(Bucket.of(a, 10).add(Bucket.of(b, 8)), Bucket.of(a, 4));

        assertThat(result.getKg(b)).isCloseTo(8 * 0.75, within(1e-12));
    }

    @Test
    void speciesOnlyObservedEntersWeightedByAlpha() {
        final Bucket result = rule.apply(Bucket.of(a, 10), Bucket.of(b, 8));

        assertThat(result.getKg(a)).isCloseTo(10 * 0.75, within(1e-12));
        assertThat(result.getKg(b)).isCloseTo(8 * 0.25, within(1e-12));
    }

    @Test
    void emptyBucketsGiveAnEmptyBucket() {
        assertThat(rule.apply(Bucket.empty(), Bucket.empty()).isEmpty()).isTrue();
    }

    @Test
    void alphaOutsideTheUnitRangeIsRejected() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new ExponentialMovingAverageOfBuckets(-0.1));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new ExponentialMovingAverageOfBuckets(1.1));
    }
}
