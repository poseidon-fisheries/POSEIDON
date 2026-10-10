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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CliqueDynamicsTest {

    private final Vessel a = activeVessel();
    private final Vessel b = activeVessel();
    private final Vessel c = activeVessel();
    private final Vessel d = activeVessel();
    private final SocialNetwork network = new SocialNetwork(() -> List.of(a, b, c, d));
    private final Map<Vessel, String> ports = new HashMap<>(Map.of(a, "X", b, "X", c, "X", d, "X"));
    private final CliqueDynamics dynamics = new CliqueDynamics(network, ports::get, 5);

    private static Vessel activeVessel() {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.isActive()).thenReturn(true);
        return vessel;
    }

    private void tieAll(final Vessel... vessels) {
        for (final Vessel source : vessels) {
            for (final Vessel recipient : vessels) {
                if (source != recipient) {
                    network.addTie(source, recipient);
                }
            }
        }
    }

    private void assertPartners(
        final Vessel vessel,
        final Vessel... partners
    ) {
        assertThat(network.getRecipients(vessel)).containsExactlyInAnyOrder(partners);
        assertThat(network.getSources(vessel)).containsExactlyInAnyOrder(partners);
    }

    @Test
    void splitsACliqueByGroupingKey() {
        tieAll(a, b, c, d);
        ports.put(c, "Y");
        ports.put(d, "Y");

        dynamics.settle(a);

        assertPartners(a, b);
        assertPartners(b, a);
        assertPartners(c, d);
        assertPartners(d, c);
    }

    @Test
    void aMemberThatMovedAloneLosesAllItsTies() {
        tieAll(a, b, c);
        ports.put(c, "Y");

        dynamics.settle(c);

        assertPartners(a, b);
        assertPartners(b, a);
        assertPartners(c);
    }

    @Test
    void anInactiveMemberLosesAllItsTies() {
        tieAll(a, b, c);
        when(b.isActive()).thenReturn(false);

        dynamics.settle(a);

        assertPartners(a, c);
        assertPartners(b);
        assertPartners(c, a);
    }

    @Test
    void leavesAnEligibleCliqueAlone() {
        tieAll(a, b, c);

        dynamics.settle(a);

        assertPartners(a, b, c);
        assertPartners(b, a, c);
        assertPartners(c, a, b);
    }

    @Test
    void rejectsAMaximumCliqueSizeBelowOne() {
        assertThatThrownBy(() -> new CliqueDynamics(network, ports::get, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
