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
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.trips.TripStartEvent;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CliqueDynamicsPropertyTest {

    private static final List<String> KEYS = List.of("X", "Y", "Z");
    private static final int MAXIMUM_FLEET_SIZE = 8;

    static void startTrip(
        final CliqueDynamics dynamics,
        final Vessel vessel
    ) {
        final Trip trip = mock(Trip.class);
        when(trip.getVessel()).thenReturn(vessel);
        dynamics.receive(new TripStartEvent(trip));
    }

    /**
     * Checks that every tie goes both ways, that no vessel is tied to itself or has more than
     * {@code maximumCliqueSize - 1} partners, and that every vessel's partners are tied to each
     * other: each partner's clique is the vessel's.
     */
    static void assertCliqueInvariants(
        final SocialNetwork network,
        final List<Vessel> fleet,
        final int maximumCliqueSize
    ) {
        for (final Vessel vessel : fleet) {
            final Set<Vessel> partners = network.getRecipients(vessel);
            assertThat(network.getSources(vessel)).isEqualTo(partners);
            assertThat(partners).doesNotContain(vessel).hasSizeLessThan(maximumCliqueSize);
            final Set<Vessel> clique = cliqueOf(network, vessel);
            for (final Vessel partner : partners) {
                assertThat(cliqueOf(network, partner)).isEqualTo(clique);
            }
        }
    }

    private static Set<Vessel> cliqueOf(
        final SocialNetwork network,
        final Vessel vessel
    ) {
        final Set<Vessel> clique = new HashSet<>(network.getRecipients(vessel));
        clique.add(vessel);
        return clique;
    }

    @Property(tries = 300)
    void cliquesStayWholeTwoWayWithinTheirMaximumSizeAndEligibleAtTripStarts(
        @ForAll @IntRange(min = 1, max = MAXIMUM_FLEET_SIZE) final int fleetSize,
        @ForAll @IntRange(min = 1, max = 5) final int maximumCliqueSize,
        @ForAll final long seed,
        @ForAll("steps") final List<Step> steps
    ) {
        final List<Vessel> fleet = new ArrayList<>();
        final Map<Vessel, String> keys = new HashMap<>();
        final Map<Vessel, Boolean> activity = new HashMap<>();
        for (int i = 0; i < fleetSize; i++) {
            final Vessel vessel = mock(Vessel.class);
            when(vessel.isActive()).thenAnswer(_ -> activity.get(vessel));
            fleet.add(vessel);
            keys.put(vessel, KEYS.getFirst());
            activity.put(vessel, true);
        }
        final SocialNetwork network = new SocialNetwork(() -> fleet);
        final CliqueDynamics dynamics = new CliqueDynamics(
            network,
            keys::get,
            maximumCliqueSize,
            new MersenneTwisterFast(seed)
        );
        for (final Step step : steps) {
            final Vessel vessel = fleet.get(step.vesselIndex() % fleetSize);
            switch (step.kind()) {
                case TRIP_START -> {
                    startTrip(dynamics, vessel);
                    assertThat(network.getRecipients(vessel)).allMatch(partner ->
                        activity.get(vessel) &&
                            activity.get(partner) &&
                            keys.get(partner).equals(keys.get(vessel))
                    );
                }
                case KEY_CHANGE -> keys.put(vessel, KEYS.get(step.keyIndex()));
                case ACTIVITY_CHANGE -> activity.put(vessel, !activity.get(vessel));
            }
            assertCliqueInvariants(network, fleet, maximumCliqueSize);
        }
    }

    @Provide
    Arbitrary<List<Step>> steps() {
        return Combinators
            .combine(
                Arbitraries.of(StepKind.class),
                Arbitraries.integers().between(0, MAXIMUM_FLEET_SIZE - 1),
                Arbitraries.integers().between(0, KEYS.size() - 1)
            )
            .as(Step::new)
            .list()
            .ofMaxSize(60);
    }

    enum StepKind { TRIP_START, KEY_CHANGE, ACTIVITY_CHANGE }

    record Step(StepKind kind, int vesselIndex, int keyIndex) {}
}
