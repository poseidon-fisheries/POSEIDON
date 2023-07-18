package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.Key;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.nio.file.Paths;
import java.util.Map;

import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class AllocationGridsTest {

    @Test
    public void AllocationGridsTester() {

        final NauticalMap nauticalMap = makeMap(3, 3);

        final AllocationGrids<Key> allocationGrids =
            new SmallLargeAllocationGridsSupplier(
                Paths.get("inputs", "epo_inputs", "tests", "mock_grids.csv"),
                nauticalMap.getMapExtent(),
                365
            ).get();

        Assertions.assertEquals(1, allocationGrids.size());

        final Map<Key, DoubleGrid2D> grid = allocationGrids.getGrids().get(0);
        final DoubleGrid2D gridLARGE = grid.get(new Key("SP1", LARGE));
        final DoubleGrid2D gridSMALL = grid.get(new Key("SP1", SMALL));

        Assertions.assertEquals(0.117031221, gridLARGE.get(0, 0), .01);
        Assertions.assertEquals(0.099199173, gridSMALL.get(0, 0), .01);
        Assertions.assertEquals(0.137690519, gridSMALL.get(2, 0), .01);

    }

}