/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

/**
 * {@link uk.ac.ox.poseidon.agents.vessels.Vessel}, the core fishing agent, and
 * {@link uk.ac.ox.poseidon.agents.vessels.Fleet}, the set of vessels operating
 * out of a set of ports. {@link uk.ac.ox.poseidon.agents.vessels.VesselScope}
 * narrows factory resolution to a single vessel. Vessels are created by the
 * {@link uk.ac.ox.poseidon.agents.vessels.VesselSpecification}, what a vessel
 * of the scenario is made of, when a source asks: a scripted one
 * ({@link uk.ac.ox.poseidon.agents.vessels.CreateVesselsAtPort}) or a
 * vessel register table
 * ({@link uk.ac.ox.poseidon.agents.vessels.VesselRegisterFactory}), whose
 * later rows change the vessels' attributes or make them exit, through the
 * fleet. Each component is declared once, by a single factory
 * ({@link uk.ac.ox.poseidon.agents.vessels.FactoryComponent}) or by a choice
 * on the vessel's attributes
 * ({@link uk.ac.ox.poseidon.agents.vessels.ChosenComponent}), which the
 * vessel picks anew whenever an attribute it reads changes.
 * A vessel's hold/gear/engine/behaviour/etc. live in sibling packages
 * ({@link uk.ac.ox.poseidon.agents.vessels.holds},
 * {@link uk.ac.ox.poseidon.agents.vessels.gears},
 * {@link uk.ac.ox.poseidon.agents.vessels.engines},
 * {@link uk.ac.ox.poseidon.agents.vessels.accounts},
 * {@link uk.ac.ox.poseidon.agents.vessels.values},
 * {@link uk.ac.ox.poseidon.agents.vessels.friends}). See
 * {@link uk.ac.ox.poseidon.agents.vessels.Factories} for this package's own
 * entry points.
 */
package uk.ac.ox.poseidon.agents.vessels;
