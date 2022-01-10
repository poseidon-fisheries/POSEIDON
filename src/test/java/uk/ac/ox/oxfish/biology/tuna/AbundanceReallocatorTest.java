package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
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
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AbundanceReallocatorTest {

    @Test
    public void AbundanceReallocatorTester(){
        Species species1 = new Species("Piano Tuna");

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);

        Map<String, IntFunction<SizeGroup>> binToSizeGroupMappings = new HashMap<>();
        binToSizeGroupMappings.put("SP1", entry -> entry==0?SMALL:LARGE );

        ImmutableList<DoubleGrid2D> grids = Stream
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

        Path INPUT_PATH = Paths.get("inputs", "epo");

        AbundanceReallocator reallocator = new AbundanceReallocator(
                new SmallLargeAllocationGridsSupplier(
                        speciesCodes,
                        INPUT_PATH.resolve("test").resolve("mock_grids.csv")   ,
                        new MapExtent(nauticalMap),
                        365).get(),
                binToSizeGroupMappings
        );

    }

}