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

package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This will create a {@link BiomassReallocator}. It will use allocation grids based on the biomass
 * distribution files that are passed to it.
 */
public class BiomassReallocatorFactory extends ReallocatorFactory<BiomassReallocator> {

    /**
     * Empty constructor needed for YAML.
     */
    @SuppressWarnings("unused")
    public BiomassReallocatorFactory() {
    }

    public BiomassReallocatorFactory(
        final InputPath biomassDistributionsFilePath,
        final int period,
        final Supplier<SpeciesCodes> speciesCodesSupplier
    ) {
        super(biomassDistributionsFilePath, period, speciesCodesSupplier);
    }

    @Override
    public BiomassReallocator apply(final FishState fishState) {
        checkNotNull(getMapExtent(), "Need to call setMapExtent() before using");
        final AllocationGrids<String> grids =
            new AllocationGridsSupplier(
                getSpeciesCodesSupplier(),
                getBiomassDistributionsFile().get(),
                getMapExtent(),
                getPeriod()
            ).get();
        return new BiomassReallocator(grids);
    }
}
