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

import lombok.Getter;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.grids.BaseDoubleGrid;
import uk.ac.ox.poseidon.geography.grids.DoubleGridWrapper;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A read-only biomass grid for a single species, meant to be shared across simulations (e.g. as
 * a global-scoped reference dataset). Unlike {@link BiomassGrid}, this does not implement
 * {@link uk.ac.ox.poseidon.geography.grids.MutableGrid}, so it never leaks a mutable handle to
 * its backing array; it is not {@link uk.ac.ox.poseidon.biology.Fisheable} either, since fishing
 * always targets a live, simulation-scoped, mutable {@link BiomassGrid} instead.
 */
public class ImmutableBiomassGrid extends DoubleGridWrapper {

    @Getter
    private final Species species;

    public ImmutableBiomassGrid(
        final ModelGrid modelGrid,
        final Species species,
        final double[][] values
    ) {
        super(new BaseDoubleGrid(modelGrid, values));
        this.species = species;
    }

}
