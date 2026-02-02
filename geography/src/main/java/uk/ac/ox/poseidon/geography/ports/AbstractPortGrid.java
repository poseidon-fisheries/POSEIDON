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

package uk.ac.ox.poseidon.geography.ports;

import lombok.Getter;
import lombok.NonNull;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;

import java.util.stream.Stream;

import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;

@Getter
public abstract class AbstractPortGrid extends ObjectGrid<Port> implements PortGrid {

    @NonNull
    protected final BathymetricGrid bathymetricGrid;

    public AbstractPortGrid(
        final ModelGrid modelGrid,
        final SparseGrid2D field,
        final @NonNull BathymetricGrid bathymetricGrid
    ) {
        super(modelGrid, field);
        this.bathymetricGrid = bathymetricGrid;
    }

    @Override
    public String getObjectId(final Port port) {
        return port.getCode();
    }

    @Override
    public Stream<Port> getPorts() {
        return bagToStream(field.getAllObjects());
    }

}
