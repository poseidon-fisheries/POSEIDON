package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.biology.tuna.BiomassReallocator.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AllocationGridsTest {

    public void test() {

        final DoubleGrid2D testGrid = new DoubleGrid2D(3, 3, 0);
//        final AllocationGrids<Map.Entry<String, SmallLargeAllocationGridsSupplier.SizeGroup>> testGrids;
//        testGrids = new ImmutableSortedMap<Integer, Map<K,DoubleGrid2D>> (1 ,testGrid);

//        final AllocationGrids<String> grids =
//                new AllocationGridsSupplier(
//                        getSpeciesCodesFilePath(),
//                        getBiomassDistributionsFilePath(),
//                        new MapExtent(fishState.getMap())
//                ).get();

    }
}