/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography.grids;

import lombok.*;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelGridWithActiveCellsFromGridFile
    extends GlobalScopeFactory<ModelGrid> {

    CellSetFromGridFileFactory cellSetFromGridFile;

    @Override
    protected ModelGrid newInstance(final @NonNull Simulation simulation) {
        final File gridFile = cellSetFromGridFile.getPath().get(simulation).toFile();
        final CoverageWrapper coverageWrapper = new CoverageWrapper(gridFile);
        return ModelGrid.withActiveCells(
            coverageWrapper.getGridWidth(),
            coverageWrapper.getGridHeight(),
            coverageWrapper.makeEnvelope(),
            cellSetFromGridFile.get(simulation)
        );
    }

}
