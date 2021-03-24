package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.Assert.assertArrayEquals;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class ScheduledBiomassRelocatorTest extends TestCase {

    public void test() {
        List<DoubleGrid2D> grids = ImmutableList.of(
            new DoubleGrid2D(new double[][]{{1, 1, 1}, {0, 0, 0}, {0, 0, 0}}),
            new DoubleGrid2D(new double[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 0}}),
            new DoubleGrid2D(new double[][]{{0, 0, 0}, {0, 0, 0}, {1, 1, 1}})
        );

        GlobalBiology globalBiology = genericListOfSpecies(2);
        NauticalMap nauticalMap = makeMap(3, 3);
        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
            seaTile.setBiology(new BiomassLocalBiology(
                new double[]{1, 1},
                new double[]{POSITIVE_INFINITY, POSITIVE_INFINITY})
            )
        );

        List<Double> initialBiomasses = getBiomasses(globalBiology, nauticalMap);
        assertEquals(initialBiomasses, ImmutableList.of(9.0, 9.0));

        ScheduledBiomassRelocator scheduledBiomassRelocator = new ScheduledBiomassRelocator(
            ImmutableMap.of(
                "Species 0", grids,
                "Species 1", Lists.reverse(grids)
            ),
            t -> t
        );

        scheduledBiomassRelocator.reallocate(globalBiology, nauticalMap, 0);

        assertEquals(getBiomasses(globalBiology, nauticalMap), ImmutableList.of(9.0, 9.0));

        assertArrayEquals(
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0)),
            new double[][]{{3, 3, 3}, {0, 0, 0}, {0, 0, 0}}
        );

        assertArrayEquals(
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)),
            new double[][]{{0, 0, 0}, {0, 0, 0}, {3, 3, 3}}
        );

        scheduledBiomassRelocator.reallocate(globalBiology, nauticalMap, 1);

        assertArrayEquals(
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)),
            new double[][]{{0, 0, 0}, {3, 3, 3}, {0, 0, 0}}
        );

        assertArrayEquals(
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)),
            new double[][]{{0, 0, 0}, {3, 3, 3}, {0, 0, 0}}
        );


        nauticalMap.getAllSeaTilesAsList().forEach(seaTile -> {
            double[] biomass = ((BiomassLocalBiology) seaTile.getBiology()).getCurrentBiomass();
            for (int i = 0; i < biomass.length; i++) biomass[i] += 1;
        });

        scheduledBiomassRelocator.reallocate(globalBiology, nauticalMap, 2);

        assertArrayEquals(
            getBiomassArray(nauticalMap, globalBiology.getSpecie(0)),
            new double[][]{{0, 0, 0}, {0, 0, 0}, {6, 6, 6}}
        );

        assertArrayEquals(
            getBiomassArray(nauticalMap, globalBiology.getSpecie(1)),
            new double[][]{{6, 6, 6}, {0, 0, 0}, {0, 0, 0}}
        );

    }

    private ImmutableList<Double> getBiomasses(GlobalBiology globalBiology, NauticalMap nauticalMap) {
        return globalBiology.getSpecies().stream().map(nauticalMap::getTotalBiomass).collect(toImmutableList());
    }

    private double[][] getBiomassArray(NauticalMap map, Species species) {
        final double[][] biomassArray = new double[map.getWidth()][map.getHeight()];
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                biomassArray[x][y] = map.getSeaTile(x, y).getBiology().getBiomass(species);
            }
        }
        return biomassArray;
    }
}