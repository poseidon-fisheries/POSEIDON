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

import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.function.Function;

/** Factories for the ties between vessels. */
public class Factories {

    private Factories() {}

    /**
     * @param candidates gives the vessels ties can be made with; pass the base fleet, not a loader
     *                   whose extra factories depend on this network, which would be a factory
     *                   cycle
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a {@link SocialNetwork}
     * with no ties
     * @see SocialNetwork
     */
    public static SocialNetworkFactory socialNetwork(
        final Factory<? super SimulationScope, ? extends VesselsGetter> candidates
    ) {
        return new SocialNetworkFactory(candidates);
    }

    /**
     * @param network           the network whose ties the dynamics add and remove
     * @param groupingKey       gives a vessel's grouping key: vessels are eligible for each other
     *                          when both are active and their keys are equal
     * @param maximumCliqueSize the largest clique a vessel joins, itself included; 1 means no
     *                          vessel ever has partners
     * @return a {@link uk.ac.ox.poseidon.io.tables.SimulationEventListenerFactory} for a
     * {@link CliqueDynamics} registered with the simulation's event manager, so that it hears
     * every vessel's trip starts
     * @see CliqueDynamics
     */
    public static CliqueDynamicsFactory cliqueDynamics(
        final Factory<? super SimulationScope, ? extends SocialNetwork> network,
        final Factory<? super SimulationScope, ? extends Function<? super Vessel, ?>> groupingKey,
        final int maximumCliqueSize
    ) {
        return new CliqueDynamicsFactory(network, groupingKey, maximumCliqueSize);
    }
}
