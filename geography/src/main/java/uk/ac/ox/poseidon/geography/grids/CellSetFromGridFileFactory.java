/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.geography.grids;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.io.File;
import java.nio.file.Path;

import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CellSetFromGridFileFactory extends GlobalScopeFactory<ImmutableSet<Int2D>> {
    private Factory<Scope, ? extends Path> path;
    private double includedValue;

    @Override
    protected ImmutableSet<Int2D> newInstance() {
        final File gridFile = path.get(GLOBAL_SCOPE).toFile();
        if (!gridFile.isFile()) {
            throw new RuntimeException(gridFile + " does not point to a file.");
        }
        final CoverageWrapper coverageWrapper = new CoverageWrapper(gridFile);
        final ImmutableSet.Builder<Int2D> builder = ImmutableSet.builder();
        coverageWrapper.processGrid((cell, value) -> {
            if (value == includedValue) {
                builder.add(cell);
            }
        });
        return builder.build();
    }
}
