package uk.ac.ox.oxfish.biology.tuna;

import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.Key;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AllocationGridsTest {

    @Test
    public void AllocationGridsTester() {
        final Species species1 = new Species("Piano Tuna");

        final Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        final Supplier<SpeciesCodes> speciesCodesSupplier = () -> new SpeciesCodes(sCodes);
        final NauticalMap nauticalMap = makeMap(3, 3);

        final AllocationGrids<Key> allocationGrids =
            new SmallLargeAllocationGridsSupplier(
                speciesCodesSupplier,
                Paths.get("inputs", "epo_inputs", "tests", "mock_grids.csv"),
                nauticalMap.getMapExtent(),
                365
            ).get();

        assertEquals(1, allocationGrids.size());

        final Map<Key, DoubleGrid2D> grid = allocationGrids.getGrids().get(0);
        final DoubleGrid2D gridLARGE = grid.get(new Key("Piano Tuna", LARGE));
        final DoubleGrid2D gridSMALL = grid.get(new Key("Piano Tuna", SMALL));

        assertEquals(0.117031221, gridLARGE.get(0, 0), .01);
        assertEquals(0.099199173, gridSMALL.get(0, 0), .01);
        assertEquals(0.137690519, gridSMALL.get(2, 0), .01);

    }

}