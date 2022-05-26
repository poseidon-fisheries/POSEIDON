package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import java.nio.file.Paths;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;

public class AbundanceReallocatorTest {

    @Test
    public void AbundanceReallocatorTester(){

        final Meristics meristics = mock(Meristics.class);
        when(meristics.getNumberOfSubdivisions()).thenReturn(2);
        when(meristics.getNumberOfBins()).thenReturn(2);

        Species species1 = new Species("Piano Tuna", meristics);

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);

        Map<String, IntFunction<SizeGroup>> binToSizeGroupMappings = new HashMap<>();
        binToSizeGroupMappings.put("Piano Tuna", entry -> entry==0?SMALL:LARGE );

/*        ImmutableList<DoubleGrid2D> grids = Stream
                .of(
                        new double[][] {{1, 1, 1}, {0, 0, 0}, {0, 0, 0}},
                        new double[][] {{0, 0, 0}, {1, 1, 1}, {0, 0, 0}},
                        new double[][] {{0, 0, 0}, {0, 0, 0}, {1, 1, 1}}
                )
                .map(DoubleGrid2D::new)
                .map(AllocationGridsSupplier::normalize)
                .collect(toImmutableList());*/
        final GlobalBiology globalBiology= new GlobalBiology(species1);
        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        final NauticalMap nauticalMap = makeMap(3, 3);
         nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
                seaTile.setBiology(new AbundanceLocalBiology(abundance)
                )
        );

        AllocationGrids<Entry<String, SizeGroup>> allocationGrids = new SmallLargeAllocationGridsSupplier(
                speciesCodes,
                TESTS_INPUT_PATH.resolve("mock_grids.csv")   ,
                new MapExtent(nauticalMap),
                365).get();

        AbundanceReallocator reallocator = new AbundanceReallocator(
                allocationGrids,
                binToSizeGroupMappings
        );




        ImmutableSortedMap<Integer, Map<Entry<String, SizeGroup>, DoubleGrid2D>> grids = allocationGrids.getGrids();
        AbundanceLocalBiology aggregatedBiology = new AbundanceLocalBiology(abundance);


//        System.out.println("break");
        reallocator.reallocate(grids.get(0), globalBiology, nauticalMap.getAllSeaTilesAsList(), aggregatedBiology);

        assertEquals(
                0.991991733,
                nauticalMap.getAllSeaTilesAsList().get(0).getAbundance(species1).asMatrix()[0][0],
                .0000001
        );

        assertEquals(
                1.13292204,
                nauticalMap.getAllSeaTilesAsList().get(4).getAbundance(species1).asMatrix()[0][1],
                .0000001
        );


    }

}