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

package uk.ac.ox.poseidon.agents.components;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VesselComponentRegisterTest {

    @Test
    void storesAndRetrievesComponents() {
        final VesselComponentRegister<String> register = new VesselComponentRegister<>();
        final Vessel vessel = mock(Vessel.class);

        register.putComponent(vessel, "component");

        assertThat(register.getComponent(vessel)).contains("component");
        assertThat(register.getVessels()).contains(vessel);
        assertThat(register.getAllEntries()).anyMatch(entry ->
            entry.getKey() == vessel && entry.getValue().equals("component")
        );
    }

    @Test
    void getOtherEntriesExcludesProvidedVessel() {
        final VesselComponentRegister<String> register = new VesselComponentRegister<>();
        final Vessel first = mock(Vessel.class);
        final Vessel second = mock(Vessel.class);

        register.putComponent(first, "first");
        register.putComponent(second, "second");

        assertThat(register.getOtherEntries(first))
            .extracting(entry -> entry.getKey())
            .containsExactly(second);
    }
}
