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

import com.google.common.collect.ImmutableSet;
import lombok.*;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CellSetFromGridFileFactory extends GlobalScopeFactory<ImmutableSet<Int2D>> {
    @NonNull private Factory<? extends Path> path;
    private double includedValue;

    @Override
    protected ImmutableSet<Int2D> newInstance(final Simulation simulation) {
        final CoverageWrapper coverageWrapper = new CoverageWrapper(path.get(simulation).toFile());
        final ImmutableSet.Builder<Int2D> builder = ImmutableSet.builder();
        coverageWrapper.processGrid((cell, value) -> {
            if (value == includedValue) {
                builder.add(cell);
            }
        });
        return builder.build();
    }
}
