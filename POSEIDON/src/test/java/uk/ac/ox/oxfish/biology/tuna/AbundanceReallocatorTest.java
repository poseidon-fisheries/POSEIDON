package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableSortedMap;
import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AbundanceReallocatorTest {

    @Test
    public void AbundanceReallocatorTester(){

        final Meristics meristics = mock(Meristics.class);
        when(meristics.getNumberOfSubdivisions()).thenReturn(2);
        when(meristics.getNumberOfBins()).thenReturn(2);

        Species species1 = new Species("Piano Tuna", meristics);

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        Supplier<SpeciesCodes> speciesCodesSupplier = () -> new SpeciesCodes(sCodes);

        final BiFunction<Species, Integer, SizeGroup> binToSizeGroup =
            (species, bin) -> bin == 0 ? SMALL : LARGE;

        final GlobalBiology globalBiology= new GlobalBiology(species1);
        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        final NauticalMap nauticalMap = makeMap(3, 3);
         nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
                seaTile.setBiology(new AbundanceLocalBiology(abundance)
                )
        );

        AllocationGrids<Entry<String, SizeGroup>> allocationGrids = new SmallLargeAllocationGridsSupplier(
            speciesCodesSupplier,
            Paths.get("inputs", "epo_inputs", "tests", "mock_grids.csv"),
            nauticalMap.getMapExtent(),
            365
        ).get();

        AbundanceReallocator reallocator = new AbundanceReallocator(
                allocationGrids,
                binToSizeGroup
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