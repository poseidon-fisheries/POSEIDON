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
 * A vessel's destination choice: {@link uk.ac.ox.poseidon.agents.choices.OptionValues} holds
 * what's been learned about each candidate option (see
 * {@link uk.ac.ox.poseidon.agents.choices.evaluation} for how those observations are produced),
 * and a {@link uk.ac.ox.poseidon.agents.choices.DestinationSupplier} (or, at a lower level, a
 * {@link uk.ac.ox.poseidon.core.providers.Provider} of a cell) picks the next destination —
 * exploring randomly or in a neighbourhood, exploiting the best-known option, imitating friends'
 * best options, or an epsilon-greedy mix of exploring and exploiting. See
 * {@link uk.ac.ox.poseidon.agents.choices.Factories} for the entry points.
 */
package uk.ac.ox.poseidon.agents.choices;
