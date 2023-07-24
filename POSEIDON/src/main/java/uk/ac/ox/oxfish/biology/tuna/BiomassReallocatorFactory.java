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

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.Reallocator.SpeciesKey;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This will create a {@link BiomassReallocator}. It will use allocation grids based on the biomass
 * distribution files that are passed to it.
 */
public class BiomassReallocatorFactory
    extends ReallocatorFactory<BiomassLocalBiology, Reallocator<BiomassLocalBiology>> {

    /**
     * Empty constructor needed for YAML.
     */
    @SuppressWarnings("unused")
    public BiomassReallocatorFactory() {
    }

    public BiomassReallocatorFactory(
        final InputPath biomassDistributionsFilePath,
        final IntegerParameter period,
        final AlgorithmFactory<MapExtent> mapExtent
    ) {
        super(biomassDistributionsFilePath, period, mapExtent);
    }

    @Override
    public BiomassReallocator apply(final FishState fishState) {
        checkNotNull(getMapExtent(), "Need to call setMapExtent() before using");
        final AllocationGrids<SpeciesKey> grids =
            new AllocationGridsSupplier(
                getBiomassDistributionsFile().get(),
                getMapExtent().apply(fishState),
                getPeriod().getValue()
            ).get();
        return new BiomassReallocator(grids);
    }
}
