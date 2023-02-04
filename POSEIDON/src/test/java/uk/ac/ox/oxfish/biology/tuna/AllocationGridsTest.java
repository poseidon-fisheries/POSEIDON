package uk.ac.ox.oxfish.biology.tuna;

import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;

public class AllocationGridsTest {

    @Test
    public void AllocationGridsTester(){
        Species species1 = new Species("Piano Tuna");

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);
        final NauticalMap nauticalMap = makeMap(3, 3);

        AllocationGrids<Map.Entry<String, SmallLargeAllocationGridsSupplier.SizeGroup>> allocationGrids = new SmallLargeAllocationGridsSupplier(
                speciesCodes,
                TESTS_INPUT_PATH.resolve("mock_grids.csv")   ,
                nauticalMap.getMapExtent(),
                365).get();


        assertEquals(1,allocationGrids.size());


 //       allocationGrids.getGrids();
        Map<Map.Entry<String, SmallLargeAllocationGridsSupplier.SizeGroup>, DoubleGrid2D> grid = allocationGrids.getGrids().get(0);
//        Object[] objects = grid.keySet().toArray();

//        Set<Map.Entry<Map.Entry<String, SmallLargeAllocationGridsSupplier.SizeGroup>, DoubleGrid2D>> entries = grid.entrySet();

 //       Set<Map.Entry<String, SmallLargeAllocationGridsSupplier.SizeGroup>> gridkeys = grid.keySet();

         DoubleGrid2D gridLARGE = grid.get(entry("Piano Tuna", LARGE));
        DoubleGrid2D gridSMALL = grid.get(entry("Piano Tuna", SMALL));
 //       System.out.println("breakpoint");

        assertEquals(0.117031221
                , gridLARGE.get(0,0), .01);
        assertEquals(0.099199173
                , gridSMALL.get(0,0), .01);
        assertEquals(0.137690519
                , gridSMALL.get(2,0), .01);

   //     System.out.println("break");


    }

}