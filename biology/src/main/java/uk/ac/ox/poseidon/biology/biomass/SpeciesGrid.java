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

package uk.ac.ox.poseidon.biology.biomass;

import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;

/**
 * A {@link DoubleGrid} for a single species, with no assumption of mutability. Both
 * {@link BiomassGrid} (live, mutable) and {@link ImmutableBiomassGrid} (read-only, shareable
 * snapshot) implement this, so code that only needs to read per-species grid values can depend on
 * this common type instead of on either concrete flavor.
 */
public interface SpeciesGrid extends DoubleGrid {
    Species getSpecies();
}
