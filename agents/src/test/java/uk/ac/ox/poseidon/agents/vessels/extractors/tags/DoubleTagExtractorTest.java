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

package uk.ac.ox.poseidon.agents.vessels.extractors.tags;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoubleTagExtractorTest {

    private static Vessel vesselWithTag(final Object tagValue) {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getTag("loa")).thenReturn(Optional.ofNullable(tagValue));
        return vessel;
    }

    @Test
    void extractsDoubleFromNumberTag() {
        final DoubleTagExtractor extractor = new DoubleTagExtractor("loa");
        assertThat(extractor.apply(vesselWithTag(12.5))).isEqualTo(12.5);
    }

    @Test
    void extractsDoubleFromNumericStringTag() {
        final DoubleTagExtractor extractor = new DoubleTagExtractor("loa");
        assertThat(extractor.apply(vesselWithTag(" 12.5 "))).isEqualTo(12.5);
    }

    @Test
    void returnsNullForNonNumericStringTag() {
        final DoubleTagExtractor extractor = new DoubleTagExtractor("loa");
        assertThat(extractor.apply(vesselWithTag("not-a-number"))).isNull();
    }

    @Test
    void returnsNullForNonFiniteNumberTag() {
        final DoubleTagExtractor extractor = new DoubleTagExtractor("loa");
        assertThat(extractor.apply(vesselWithTag(Double.NaN))).isNull();
        assertThat(extractor.apply(vesselWithTag(Double.POSITIVE_INFINITY))).isNull();
    }

    @Test
    void returnsNullWhenTagIsAbsent() {
        final DoubleTagExtractor extractor = new DoubleTagExtractor("loa");
        assertThat(extractor.apply(vesselWithTag(null))).isNull();
    }
}
