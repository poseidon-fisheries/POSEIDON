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

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class AllocationGridsTest {

    @Test
    public void AllocationGridsTester(){
        Species species1 = new Species("Piano Tuna");

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);
        final NauticalMap nauticalMap = makeMap(3, 3);

        Path INPUT_PATH = Paths.get("inputs", "epo");
        AllocationGrids<Map.Entry<String, SmallLargeAllocationGridsSupplier.SizeGroup>> allocationGrids = new SmallLargeAllocationGridsSupplier(
                speciesCodes,
                INPUT_PATH.resolve("test").resolve("mock_grids.csv")   ,
                new MapExtent(nauticalMap),
                365).get();


        assertEquals(1,allocationGrids.size());

//        DoubleGrid2D daGrid = allocationGrids.getGrids().get(0).get(entry(species1, ));

//        assertEquals(3, allocationGrids.getGrids().get(0).get(entry(species1, "LARGE")).get(0,0),.01);
    }

}