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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertArrayEquals;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;

public class BiomassReallocatorTest extends TestCase {

    public void test() {
        final ImmutableList<DoubleGrid2D> grids = Stream
            .of(
                new double[][] {{1, 1, 1}, {0, 0, 0}, {0, 0, 0}},
                new double[][] {{0, 0, 0}, {1, 1, 1}, {0, 0, 0}},
                new double[][] {{0, 0, 0}, {0, 0, 0}, {1, 1, 1}}
            )
            .map(DoubleGrid2D::new)
            .map(AllocationGridsSupplier::normalize)
            .collect(toImmutableList());

        final GlobalBiology globalBiology = genericListOfSpecies(2);
        final NauticalMap nauticalMap = makeMap(3, 3);
        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
            seaTile.setBiology(new BiomassLocalBiology(
                    new double[] {1, 1},
                    new double[] {POSITIVE_INFINITY, POSITIVE_INFINITY}
                )
            )
        );

        final List<Double> initialBiomasses = getBiomasses(globalBiology, nauticalMap);
        assertEquals(ImmutableList.of(9.0, 9.0), initialBiomasses);

        final BiomassReallocator biomassReallocator = new BiomassReallocator(
            AllocationGrids.from(
                range(0, grids.size()).boxed().collect(toImmutableMap(
                    identity(),
                    i -> globalBiology.getSpecies().stream().collect(toImmutableMap(
                        Species::getName,
                        species -> (species.getIndex() == 0 ? grids : grids.reverse()).get(i)
                    ))
                )),
                3
            )
        );

        final BiomassAggregator biomassAggregator = new BiomassAggregator();
        final BiomassLocalBiology aggregatedBiology =
            biomassAggregator.aggregate(globalBiology, nauticalMap, null);

        biomassReallocator.reallocate(
            0,
            globalBiology,
            nauticalMap.getAllSeaTilesAsList(),
            aggregatedBiology
        );

        assertEquals(
            ImmutableList.of(9.0, 9.0),
            getBiomasses(globalBiology, nauticalMap)
        );

        assertArrayEquals(
            new double[][] {{3, 3, 3}, {0, 0, 0}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0))
        );

        assertArrayEquals(
            new double[][] {{0, 0, 0}, {0, 0, 0}, {3, 3, 3}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1))
        );

        biomassReallocator.reallocate(
            1,
            globalBiology,
            nauticalMap.getAllSeaTilesAsList(),
            aggregatedBiology
        );

        assertArrayEquals(
            new double[][] {{0, 0, 0}, {3, 3, 3}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1))
        );

        assertArrayEquals(
            new double[][] {{0, 0, 0}, {3, 3, 3}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1))
        );

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
            aggregatedBiology
        );

        assertArrayEquals(
            new double[][] {{0, 0, 0}, {0, 0, 0}, {6, 6, 6}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0))
        );

        assertArrayEquals(
            new double[][] {{6, 6, 6}, {0, 0, 0}, {0, 0, 0}},
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1))
        );

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