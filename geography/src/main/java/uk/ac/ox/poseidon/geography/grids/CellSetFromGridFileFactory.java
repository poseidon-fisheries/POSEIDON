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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.io.File;
import java.nio.file.Path;

import static lombok.AccessLevel.PACKAGE;

/**
 * A {@link RelativeScopeFactory} that reads a raster grid file and returns the set of cells whose
 * value equals a fixed target — e.g. picking out land cells from a land/water raster. There's no
 * separate plain component class here: the produced {@link ImmutableSet} is returned as-is, with
 * no wrapper type to carry documentation, so this factory carries the behavior doc directly.
 * Built via {@link Factories#cellSetFromGridFile(Factory, double)} in this package.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = true)
public class CellSetFromGridFileFactory<S extends Scope>
    extends RelativeScopeFactory<S, ImmutableSet<Int2D>> {

    private Factory<? super S, ? extends Path> path;
    private double includedValue;

    /** @throws RuntimeException if {@code path} doesn't point to a file */
    @Override
    protected ImmutableSet<Int2D> newInstance(final S scope) {
        final File gridFile = path.get(scope).toFile();
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
