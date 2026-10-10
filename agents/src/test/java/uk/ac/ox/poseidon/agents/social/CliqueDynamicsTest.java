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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.social.CliqueDynamicsPropertyTest.assertCliqueInvariants;
import static uk.ac.ox.poseidon.agents.social.CliqueDynamicsPropertyTest.startTrip;

class CliqueDynamicsTest {

    private final Vessel a = activeVessel();
    private final Vessel b = activeVessel();
    private final Vessel c = activeVessel();
    private final Vessel d = activeVessel();
    private final SocialNetwork network = new SocialNetwork(() -> List.of(a, b, c, d));
    private final Map<Vessel, String> ports = new HashMap<>(Map.of(a, "X", b, "X", c, "X", d, "X"));
    private final CliqueDynamics dynamics = dynamics(5);

    private static Vessel activeVessel() {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.isActive()).thenReturn(true);
        return vessel;
    }

    private CliqueDynamics dynamics(final int maximumCliqueSize) {
        return new CliqueDynamics(
            network,
            ports::get,
            maximumCliqueSize,
            new MersenneTwisterFast(0)
        );
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
        assertThatThrownBy(() -> dynamics(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aLoneVesselJoinsAWholeCliqueWithRoom() {
        tieAll(a, b, c);

        dynamics.join(d);

        assertPartners(d, a, b, c);
        assertPartners(a, b, c, d);
    }

    @Test
    void aFullCliqueIsNotJoined() {
        tieAll(a, b, c);

        dynamics(3).join(d);

        assertPartners(d);
        assertPartners(a, b, c);
    }

    @Test
    void twoLoneVesselsPairUp() {
        ports.put(c, "Y");
        ports.put(d, "Y");

        dynamics.join(a);

        assertPartners(a, b);
        assertPartners(b, a);
    }

    @Test
    void aVesselWithNoEligibleVesselWithRoomStaysAlone() {
        ports.put(b, "Y");
        ports.put(c, "Y");
        when(d.isActive()).thenReturn(false);

        dynamics.join(a);

        assertPartners(a);
    }

    @Test
    void aVesselWithPartnersDoesNotJoin() {
        tieAll(a, b);

        dynamics.join(a);

        assertPartners(a, b);
        assertPartners(c);
        assertPartners(d);
    }

    @Test
    void aVesselAloneInTheFleetStaysAlone() {
        final SocialNetwork network = new SocialNetwork(() -> List.of(a));
        final CliqueDynamics dynamics =
            new CliqueDynamics(network, ports::get, 5, new MersenneTwisterFast(0));

        dynamics.join(a);

        assertThat(network.getRecipients(a)).isEmpty();
        assertThat(network.getSources(a)).isEmpty();
    }

    @Test
    void withAMaximumCliqueSizeOfOneNoVesselJoins() {
        final CliqueDynamics dynamics = dynamics(1);

        List.of(a, b, c, d).forEach(dynamics::join);

        List.of(a, b, c, d).forEach(this::assertPartners);
    }

    @Test
    void joiningSettlesTheChosenClique() {
        tieAll(a, b, c);
        ports.put(c, "Y");

        dynamics(4).join(d);

        assertPartners(d, a, b);
        assertPartners(c);
    }

    @Test
    void theSameSeedAndTripStartsGiveTheSameCliques() {
        assertThat(cliquesAfterJoining(8)).isEqualTo(cliquesAfterJoining(8));
    }

    /**
     * @return the partners of each of a fresh fleet's vessels, as indices in the fleet, after
     * each vessel joins in turn
     */
    private List<Set<Integer>> cliquesAfterJoining(final int fleetSize) {
        final List<Vessel> fleet = new ArrayList<>();
        for (int i = 0; i < fleetSize; i++) {
            fleet.add(activeVessel());
        }
        final SocialNetwork network = new SocialNetwork(() -> fleet);
        final CliqueDynamics dynamics =
            new CliqueDynamics(network, vessel -> "X", 3, new MersenneTwisterFast(42));
        fleet.forEach(dynamics::join);
        return fleet
            .stream()
            .map(vessel -> network
                .getRecipients(vessel)
                .stream()
                .map(fleet::indexOf)
                .collect(toSet()))
            .toList();
    }

    @Test
    void aVesselLeftAloneBySettlingJoinsInTheSameStep() {
        tieAll(a, b);
        ports.put(a, "Y");
        ports.put(c, "Y");
        ports.put(d, "Z");

        startTrip(dynamics, a);

        assertPartners(a, c);
        assertPartners(b);
    }

    @Test
    void aPortChangeBetweenTwoTripStartsIsSeenAtTheSecond() {
        tieAll(a, b);
        ports.put(c, "Y");
        ports.put(d, "Y");
        startTrip(dynamics, a);
        assertPartners(a, b);

        ports.put(b, "Z");
        startTrip(dynamics, a);

        assertPartners(a);
        assertPartners(b);
    }

    @Test
    void vesselsMovingIntoAFullCliquesPortNeitherOverfillItNorLeaveOneWayTies() {
        // The 2026-10-09 review's case, with A, B, D, E and F as a, b, c, d and e.
        final Vessel e = activeVessel();
        final List<Vessel> fleet = List.of(a, b, c, d, e);
        final SocialNetwork network = new SocialNetwork(() -> fleet);
        final CliqueDynamics dynamics =
            new CliqueDynamics(network, ports::get, 3, new MersenneTwisterFast(0));
        ports.putAll(Map.of(a, "P", b, "P", c, "Q", d, "Q", e, "Q"));
        network.addTie(a, b);
        network.addTie(b, a);
        network.addTie(c, d);
        network.addTie(d, c);

        ports.put(a, "Q");
        startTrip(dynamics, a);
        ports.put(b, "Q");
        startTrip(dynamics, b);
        startTrip(dynamics, e);

        assertCliqueInvariants(network, fleet, 3);
    }
}
