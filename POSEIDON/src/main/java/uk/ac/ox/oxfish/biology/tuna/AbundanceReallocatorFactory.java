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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;

/**
 * Creates an {@link AbundanceReallocator}. The mildly peculiar thing here is that we use the weight groups from the
 * tuna meristics to create the "bin to size category" mapping. It's just a way to tell the factory where the cutoff
 * between "small" and "large" is and let the factory build the relevant function. Note that this is very tuna specific.
 * We'd need to make this class a bit more general if it was to be used with different groupings or meristics classes.
 */
public class AbundanceReallocatorFactory
    extends ReallocatorFactory<AbundanceLocalBiology, Reallocator<AbundanceLocalBiology>> {

    /**
     * Empty constructor needed for YAML.
     */
    @SuppressWarnings("unused")
    public AbundanceReallocatorFactory() {
    }

    public AbundanceReallocatorFactory(
        final InputPath biomassDistributionsFilePath,
        final int period
    ) {
        super(biomassDistributionsFilePath, period);
    }

    @Override
    public AbundanceReallocator apply(final FishState fishState) {
        checkNotNull(getMapExtent(), "Need to call setMapExtent() before using");

        final AllocationGrids<SmallLargeAllocationGridsSupplier.Key> grids =
            new SmallLargeAllocationGridsSupplier(
                getBiomassDistributionsFile().get(),
                getMapExtent(),
                365
            ).get();

        final BiFunction<Species, Integer, SizeGroup> binToSizeGroup =
            (species, bin) -> {
                final TunaMeristics meristics = (TunaMeristics) species.getMeristics();
                final List<Map<String, List<Integer>>> weightBins = meristics.getWeightBins();
                final Map<String, List<Integer>> stringListMap = weightBins.get(0);
                final List<Integer> large = stringListMap.get("large");
                final int firstLargeBin = large.get(0);
                return bin >= firstLargeBin ? LARGE : SMALL;
            };

        return new AbundanceReallocator(grids, binToSizeGroup);
    }
}
