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

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.MasonUtils.upToOneOf;

/**
 * Network dynamics that keep vessels in cliques of at most {@link #maximumCliqueSize} vessels
 * eligible for each other: both active, with equal grouping keys (in NW Med, the same home port
 * and gear). Every tie goes both ways, so a vessel's clique is itself and its partners, its
 * recipients in the network.
 */
public class CliqueDynamics {

    private final SocialNetwork network;
    private final Function<? super Vessel, ?> groupingKey;
    private final int maximumCliqueSize;
    private final MersenneTwisterFast rng;

    CliqueDynamics(
        final SocialNetwork network,
        final Function<? super Vessel, ?> groupingKey,
        final int maximumCliqueSize,
        final MersenneTwisterFast rng
    ) {
        checkArgument(
            maximumCliqueSize >= 1,
            "The maximum clique size must be at least 1, not %s.", maximumCliqueSize
        );
        this.network = checkNotNull(network);
        this.groupingKey = checkNotNull(groupingKey);
        this.maximumCliqueSize = maximumCliqueSize;
        this.rng = checkNotNull(rng);
    }

    /**
     * Removes every tie between two members of the vessel's clique that are not eligible for each
     * other. Eligibility being transitive, the clique splits into cliques of eligible vessels.
     */
    /**
     * If the vessel is alone, picks at random, among the network's candidates, a vessel eligible
     * for it whose clique has room, settles that clique and ties the vessel, both ways, to every
     * member left. A vessel with partners, or with no such vessel to pick, is left as it is. The
     * pick depends only on the random number generator and the order of the candidates, so runs
     * with the same seed and candidates in the same order make the same cliques.
     */
    void join(final Vessel vessel) {
        if (!network.getRecipients(vessel).isEmpty()) {
            return;
        }
        final List<Vessel> eligibleVesselsWithRoomInTheirClique = network
            .getCandidates()
            .stream()
            .filter(candidate -> candidate != vessel)
            .filter(candidate -> areEligible(vessel, candidate))
            .filter(candidate -> network.getRecipients(candidate).size() + 1 < maximumCliqueSize)
            .toList();
        upToOneOf(eligibleVesselsWithRoomInTheirClique, rng).ifPresent(chosenVessel -> {
            settle(chosenVessel);
            cliqueOf(chosenVessel).forEach(member -> tie(vessel, member));
        });
    }

    void settle(final Vessel vessel) {
        final List<Vessel> clique = cliqueOf(vessel);
        for (int i = 0; i < clique.size(); i++) {
            for (int j = i + 1; j < clique.size(); j++) {
                if (!areEligible(clique.get(i), clique.get(j))) {
                    untie(clique.get(i), clique.get(j));
                }
            }
        }
    }

    private List<Vessel> cliqueOf(final Vessel vessel) {
        return ImmutableList.<Vessel>builder()
            .add(vessel)
            .addAll(network.getRecipients(vessel))
            .build();
    }

    private boolean areEligible(
        final Vessel a,
        final Vessel b
    ) {
        return a.isActive() &&
            b.isActive() &&
            Objects.equals(groupingKey.apply(a), groupingKey.apply(b));
    }

    private void tie(
        final Vessel a,
        final Vessel b
    ) {
        network.addTie(a, b);
        network.addTie(b, a);
    }

    private void untie(
        final Vessel a,
        final Vessel b
    ) {
        network.removeTie(a, b);
        network.removeTie(b, a);
    }
}
