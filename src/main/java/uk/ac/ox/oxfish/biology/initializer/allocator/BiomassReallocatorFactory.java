/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import java.nio.file.Path;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class BiomassReallocatorFactory
    extends ReallocatorFactory
    implements AlgorithmFactory<BiomassReallocator> {

    public BiomassReallocatorFactory() {
    }

    public BiomassReallocatorFactory(
        final Path speciesCodesFilePath,
        final Path biomassDistributionsFilePath,
        final int period
    ) {
        super(speciesCodesFilePath, biomassDistributionsFilePath, period);
    }

    @Override
    public BiomassReallocator apply(final FishState fishState) {
        final AllocationGrids<String> grids =
            new AllocationGridsSupplier(
                getSpeciesCodesFilePath(),
                getBiomassDistributionsFilePath(),
                new MapExtent(fishState.getMap())
            ).get();
        return new BiomassReallocator(grids, getPeriod());
    }
}
