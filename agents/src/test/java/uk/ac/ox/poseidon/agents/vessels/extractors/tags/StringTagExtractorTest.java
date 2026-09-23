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

class StringTagExtractorTest {

    private static Vessel vesselWithTag(final Object tagValue) {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getTag("country_of_registration")).thenReturn(Optional.ofNullable(tagValue));
        return vessel;
    }

    @Test
    void extractsTrimmedStringTag() {
        final StringTagExtractor extractor = new StringTagExtractor("country_of_registration");
        assertThat(extractor.apply(vesselWithTag(" FRA "))).isEqualTo("FRA");
    }

    @Test
    void returnsNullForBlankOrNaTag() {
        final StringTagExtractor extractor = new StringTagExtractor("country_of_registration");
        assertThat(extractor.apply(vesselWithTag("  "))).isNull();
        assertThat(extractor.apply(vesselWithTag("NA"))).isNull();
    }

    @Test
    void returnsNullWhenTagIsAbsent() {
        final StringTagExtractor extractor = new StringTagExtractor("country_of_registration");
        assertThat(extractor.apply(vesselWithTag(null))).isNull();
    }
}
