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
 * Behavior-tree tasks for moving a vessel and running its trips end to end: travelling along a
 * path or directly to a destination, refuelling, turning back, ending a trip, and
 * {@link uk.ac.ox.poseidon.agents.tasks.travel.RoundTripFactory} for stringing a whole trip
 * together out of these and the fishing/landing tasks from sibling packages. See
 * {@link uk.ac.ox.poseidon.agents.tasks.travel.Factories} for the entry points.
 */
package uk.ac.ox.poseidon.agents.tasks.travel;
