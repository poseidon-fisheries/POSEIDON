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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import org.jetbrains.annotations.NotNull;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A {@link Reallocator} class where the local biology is abundance based. The type of key used to
 * identify the right allocation map is a combination of species name and size group. In the case of
 * tuna in the EPO, for example, we have different map for "small" and "large" tunas of each
 * species. To know which map to use for each age bin, objects of this class must be pass a function
 * that associates each bin to the right map.
 */
public class AbundanceReallocator
    extends Reallocator<Entry<String, SizeGroup>, AbundanceLocalBiology> {

    private final Map<String, ? extends IntFunction<SizeGroup>> binToSizeGroupMappings;

    /**
     * Constructs a new AbundanceReallocator.
     *
     * @param allocationGrids        The distribution grids used to reallocate biomass
     * @param binToSizeGroupMappings A map from species names to a function giving us the size group
     *                               (small or large) for each age bin
     */
    AbundanceReallocator(
        final AllocationGrids<Entry<String, SizeGroup>> allocationGrids,
        final Map<String, ? extends IntFunction<SizeGroup>> binToSizeGroupMappings
    ) {
        super(allocationGrids, new AbundanceAggregator());
        this.binToSizeGroupMappings = ImmutableMap.copyOf(binToSizeGroupMappings);
    }

    @Override
    public void reallocate(
        final Map<Entry<String, SizeGroup>, DoubleGrid2D> allocationGrids,
        final GlobalBiology globalBiology,
        final List<SeaTile> seaTiles,
        final AbundanceLocalBiology aggregatedBiology
    ) {
        final Map<Species, DoubleGrid2D[]> gridsPerSpecies =
            getGrids(globalBiology, allocationGrids);
        seaTiles.forEach(seaTile -> {
            final LocalBiology biology = seaTile.getBiology();
            final Int2D xy = seaTile.getGridLocation();
            for (final Species species : globalBiology.getSpecies()) {
                final double[][] aggregatedMatrix =
                    aggregatedBiology.getAbundance(species).asMatrix();
                final double[][] localMatrix =
                    biology.getAbundance(species).asMatrix();
                final DoubleGrid2D[] grids = gridsPerSpecies.get(species);
                final int numSubs = species.getNumberOfSubdivisions();
                final int numBins = species.getNumberOfBins();
                for (int sub = 0; sub < numSubs; sub++) {
                    for (int bin = 0; bin < numBins; bin++) {
                        final double numFish = aggregatedMatrix[sub][bin];
                        localMatrix[sub][bin] = numFish * grids[bin].get(xy.x, xy.y);
                    }
                }
            }
        });
    }

    @NotNull
    private Map<Species, DoubleGrid2D[]> getGrids(
        final GlobalBiology globalBiology,
        final Map<Entry<String, SizeGroup>, DoubleGrid2D> allocationGrids
    ) {
        return globalBiology.getSpecies().stream().collect(toImmutableMap(
            identity(),
            species -> {
                final String speciesName = species.getName();
                final IntFunction<SizeGroup> getSizeGroup = binToSizeGroupMappings.get(speciesName);
                return range(0, species.getNumberOfBins())
                    .mapToObj(bin -> allocationGrids.get(entry(
                        speciesName,
                        getSizeGroup.apply(bin)
                    )))
                    .toArray(DoubleGrid2D[]::new);
            }
        ));
    }

}