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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

class ExponentialMovingAverageOfDoublesTest {

    @Test
    void alphaOfOneReplacesWhatIsRemembered() {
        assertThat(new ExponentialMovingAverageOfDoubles(1).apply(10.0, 4.0)).isEqualTo(4.0);
    }

    @Test
    void alphaOfZeroKeepsWhatIsRemembered() {
        assertThat(new ExponentialMovingAverageOfDoubles(0).apply(10.0, 4.0)).isEqualTo(10.0);
    }

    @Test
    void intermediateAlphaWeighsTheObservationByAlpha() {
        assertThat(new ExponentialMovingAverageOfDoubles(0.25).apply(10.0, 4.0))
            .isCloseTo(10.0 * 0.75 + 4.0 * 0.25, within(1e-12));
    }

    @Test
    void alphaOutsideTheUnitRangeIsRejected() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new ExponentialMovingAverageOfDoubles(-0.1));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new ExponentialMovingAverageOfDoubles(1.1));
    }
}
