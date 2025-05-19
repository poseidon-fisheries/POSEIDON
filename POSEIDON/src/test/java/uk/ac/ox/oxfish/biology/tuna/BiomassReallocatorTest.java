/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.tuna.Reallocator.SpeciesKey;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class BiomassReallocatorTest {

    @Test
    public void test() {
        final ImmutableList<DoubleGrid2D> grids = Stream
            .of(
                new double[][]{{1, 1, 1}, {0, 0, 0}, {0, 0, 0}},
                new double[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 0}},
                new double[][]{{0, 0, 0}, {0, 0, 0}, {1, 1, 1}}
            )
            .map(DoubleGrid2D::new)
            .map(AllocationGridsSupplier::normalize)
            .collect(toImmutableList());

        final GlobalBiology globalBiology = genericListOfSpecies(2);
        final NauticalMap nauticalMap = makeMap(3, 3);
        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
            seaTile.setBiology(new BiomassLocalBiology(
                    new double[]{1, 1},
                    new double[]{POSITIVE_INFINITY, POSITIVE_INFINITY}
                )
            )
        );

        final List<Double> initialBiomasses = getBiomasses(globalBiology, nauticalMap);
        Assertions.assertEquals(ImmutableList.of(9.0, 9.0), initialBiomasses);

        final BiomassReallocator biomassReallocator = new BiomassReallocator(
            AllocationGrids.from(
                range(0, grids.size()).boxed().collect(toImmutableMap(
                    identity(),
                    i -> globalBiology.getSpecies().stream().collect(toImmutableMap(
                        species -> new SpeciesKey(species.getName()),
                        species -> (species.getIndex() == 0 ? grids : grids.reverse()).get(i)
                    ))
                )),
                3
            )
        );

        final FishState fishState = mock(FishState.class);
        when(fishState.getMap()).thenReturn(nauticalMap);
        when(fishState.getFadMap()).thenReturn(null);
        when(fishState.getBiology()).thenReturn(globalBiology);
        final BiomassAggregator biomassAggregator = new BiomassAggregator();
        final BiomassExtractor biomassExtractor = new BiomassExtractor(false, true);

        biomassReallocator.reallocate(
            0,
            globalBiology,
            nauticalMap.getAllSeaTilesAsList(),
            biomassAggregator.apply(fishState.getBiology(), biomassExtractor.apply(fishState))
        );

        Assertions.assertEquals(ImmutableList.of(9.0, 9.0), getBiomasses(globalBiology, nauticalMap));

        Assertions.assertArrayEquals(new double[][]{{3, 3, 3}, {0, 0, 0}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0)));

        Assertions.assertArrayEquals(new double[][]{{0, 0, 0}, {0, 0, 0}, {3, 3, 3}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)));

        biomassReallocator.reallocate(
            1,
            globalBiology,
            nauticalMap.getAllSeaTilesAsList(),
            biomassAggregator.apply(fishState.getBiology(), biomassExtractor.apply(fishState))
        );

        Assertions.assertArrayEquals(new double[][]{{0, 0, 0}, {3, 3, 3}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0)));

        Assertions.assertArrayEquals(new double[][]{{0, 0, 0}, {3, 3, 3}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)));

        nauticalMap.getAllSeaTilesAsList().forEach(seaTile -> {
            final double[] biomass =
                ((VariableBiomassBasedBiology) seaTile.getBiology()).getCurrentBiomass();
            for (int i = 0; i < biomass.length; i++) {
                biomass[i] += 1;
            }
        });

        biomassReallocator.reallocate(
            2,
            globalBiology,
            nauticalMap.getAllSeaTilesAsList(),
            biomassAggregator.apply(fishState.getBiology(), biomassExtractor.apply(fishState))
        );

        Assertions.assertArrayEquals(new double[][]{{0, 0, 0}, {0, 0, 0}, {6, 6, 6}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0)));

        Assertions.assertArrayEquals(new double[][]{{6, 6, 6}, {0, 0, 0}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)));

    }

    private static ImmutableList<Double> getBiomasses(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap
    ) {
        return globalBiology.getSpecies().stream().map(nauticalMap::getTotalBiomass)
            .collect(toImmutableList());
    }

    private static double[][] getBiomassArray(final NauticalMap map, final Species species) {
        final double[][] biomassArray = new double[map.getWidth()][map.getHeight()];
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                biomassArray[x][y] = map.getSeaTile(x, y).getBiology().getBiomass(species);
            }
        }
        return biomassArray;
    }
}
