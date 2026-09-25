/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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
 * What happens to a {@link uk.ac.ox.poseidon.agents.catches.CategorisedCatch} once it's caught:
 * one or more {@link uk.ac.ox.poseidon.agents.catches.disposition.DispositionProcess} steps sort
 * it into retained/discarded-alive/discarded-dead
 * ({@link uk.ac.ox.poseidon.agents.catches.disposition.Disposition}), chained via
 * {@link uk.ac.ox.poseidon.agents.catches.disposition.CompositeDispositionProcess}. See
 * {@link uk.ac.ox.poseidon.agents.catches.disposition.Factories} for the entry points.
 */
package uk.ac.ox.poseidon.agents.catches.disposition;
