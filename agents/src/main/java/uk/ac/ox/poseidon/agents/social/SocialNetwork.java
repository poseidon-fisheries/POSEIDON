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

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;

import java.util.Collection;
import java.util.Set;

import static lombok.AccessLevel.PACKAGE;

/**
 * Directed ties between vessels, where a tie from A to B means that A tells B. The network is
 * passive: it holds the ties and a live source of candidate vessels, and knows nothing of what
 * is told through the ties or of what adds and removes them (network dynamics do that). A tie
 * from a vessel to itself is rejected.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class SocialNetwork {

    private final MutableGraph<Vessel> ties =
        GraphBuilder.directed().allowsSelfLoops(false).build();
    private final VesselsGetter candidates;

    /** @return the vessels the given vessel tells; empty if it has no ties */
    public Set<Vessel> getRecipients(final Vessel vessel) {
        return ties.nodes().contains(vessel)
            ? ImmutableSet.copyOf(ties.successors(vessel))
            : Set.of();
    }

    /** @return the vessels that tell the given vessel; empty if it has no ties */
    public Set<Vessel> getSources(final Vessel vessel) {
        return ties.nodes().contains(vessel)
            ? ImmutableSet.copyOf(ties.predecessors(vessel))
            : Set.of();
    }

    /** @return the vessels ties can currently be made with, read from their source at each call */
    public Collection<Vessel> getCandidates() {
        return candidates.getVessels();
    }

    /** Adds a tie through which {@code source} tells {@code recipient}. */
    public void addTie(
        final Vessel source,
        final Vessel recipient
    ) {
        ties.putEdge(source, recipient);
    }

    /** Removes the tie through which {@code source} tells {@code recipient}, if there is one. */
    public void removeTie(
        final Vessel source,
        final Vessel recipient
    ) {
        ties.removeEdge(source, recipient);
    }
}
