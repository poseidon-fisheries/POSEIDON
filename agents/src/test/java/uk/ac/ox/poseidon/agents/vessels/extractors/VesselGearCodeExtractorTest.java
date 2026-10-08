/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.extractors;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VesselGearCodeExtractorTest {

    private static Vessel vesselWithGearCode(final String gearCode) {
        final Gear gear = mock(Gear.class);
        when(gear.getCode()).thenReturn(gearCode);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getGear()).thenReturn(gear);
        return vessel;
    }

    @Test
    void extractsTheCodeOfTheVesselsCurrentGear() {
        assertThat(new VesselGearCodeExtractor().apply(vesselWithGearCode("OTB")))
            .isEqualTo("OTB");
    }

    @Test
    void extractsNullForAGearWithoutCode() {
        assertThat(new VesselGearCodeExtractor().apply(vesselWithGearCode(null))).isNull();
    }
}
