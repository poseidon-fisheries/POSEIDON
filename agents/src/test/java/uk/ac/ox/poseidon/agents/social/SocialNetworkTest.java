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
package uk.ac.ox.poseidon.agents.social;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class SocialNetworkTest {

    private final Vessel a = mock(Vessel.class);
    private final Vessel b = mock(Vessel.class);
    private final List<Vessel> vessels = new ArrayList<>(List.of(a, b));
    private final SocialNetwork network = new SocialNetwork(() -> vessels);

    @Test
    void aTieGoesFromItsSourceToItsRecipientOnly() {
        network.addTie(a, b);

        assertThat(network.getRecipients(a)).containsExactly(b);
        assertThat(network.getRecipients(b)).isEmpty();
        assertThat(network.getSources(b)).containsExactly(a);
        assertThat(network.getSources(a)).isEmpty();
    }

    @Test
    void aVesselNeverTiedHasNoRecipientsAndNoSources() {
        assertThat(network.getRecipients(a)).isEmpty();
        assertThat(network.getSources(a)).isEmpty();
    }

    @Test
    void removingATieRemovesOnlyThatDirection() {
        network.addTie(a, b);
        network.addTie(b, a);

        network.removeTie(a, b);

        assertThat(network.getRecipients(a)).isEmpty();
        assertThat(network.getRecipients(b)).containsExactly(a);
    }

    @Test
    void rejectsATieFromAVesselToItself() {
        assertThatThrownBy(() -> network.addTie(a, a))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readsItsCandidatesAtEachCall() {
        final Vessel c = mock(Vessel.class);
        vessels.add(c);

        assertThat(network.getCandidates()).containsExactly(a, b, c);
    }
}
