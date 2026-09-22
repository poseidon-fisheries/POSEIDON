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

/**
 * {@link uk.ac.ox.poseidon.biology.species.Species} identity, and the array-backed addressing
 * scheme ({@link uk.ac.ox.poseidon.biology.species.SpeciesIndex}, {@code SpeciesIndexed*}) used
 * across the model to store per-species data without species-keyed maps. See
 * {@link uk.ac.ox.poseidon.biology.species.Factories} for the entry points. The
 * {@code extractors} subpackage builds simple {@link java.util.function.Function}s that pull a
 * field off a {@link uk.ac.ox.poseidon.biology.species.Species}.
 */
package uk.ac.ox.poseidon.biology.species;
