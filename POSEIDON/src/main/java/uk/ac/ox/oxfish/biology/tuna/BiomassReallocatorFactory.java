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
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

/**
 * This will create a {@link BiomassReallocator}. It will use allocation grids based on the biomass distribution files
 * that are passed to it.
 */
public class BiomassReallocatorFactory
    extends ReallocatorFactory<BiomassLocalBiology, Reallocator<BiomassLocalBiology>> {

    private final CacheByFishState<BiomassReallocator> cache = new CacheByFishState<>(fishState -> {
        final AllocationGrids<Reallocator.SpeciesKey> grids =
            new AllocationGridsSupplier(
                getBiomassDistributionsFile().get(),
                getMapExtent().apply(fishState),
                getPeriod().getValue()
            ).get();
        return new BiomassReallocator(grids);
    });

    /**
     * Empty constructor needed for YAML.
     */
    @SuppressWarnings("unused")
    public BiomassReallocatorFactory() {
    }

    public BiomassReallocatorFactory(
        final InputPath biomassDistributionsFilePath,
        final IntegerParameter period,
        final ComponentFactory<MapExtent> mapExtent
    ) {
        super(biomassDistributionsFilePath, period, mapExtent);
    }

    @Override
    public BiomassReallocator apply(final FishState fishState) {
        return cache.get(fishState);
    }
}
