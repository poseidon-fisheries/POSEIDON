package uk.ac.ox.oxfish.biology.initializer.allocator;

import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class ScheduledBiomassReallocatorInitializerFactoryTest extends TestCase {

    public void testBuildBiomassGrids() {
        ScheduledBiomassReallocatorInitializerFactory scheduledBiomassReallocatorInitializerFactory = new ScheduledBiomassReallocatorInitializerFactory();
        scheduledBiomassReallocatorInitializerFactory.setStartDate("2017-01-01");
        scheduledBiomassReallocatorInitializerFactory.setEndDate("2017-12-31");
        Path depthFile = TunaScenario.input("depth.csv");
        MapInitializer mapInitializer = new FromFileMapInitializer(depthFile, 101, 0.5, true, true);
        NauticalMap nauticalMap = mapInitializer.makeMap(null, null, null);
        ScheduledBiomassRelocator scheduledBiomassRelocator = scheduledBiomassReallocatorInitializerFactory.scheduledBiomassRelocator();
        assertEquals(364, scheduledBiomassRelocator.getPeriod());
        assertEquals(12, scheduledBiomassRelocator.getBiomassGrids().size());
        Collection<Map<String, DoubleGrid2D>> biomassGrids = scheduledBiomassRelocator.getBiomassGrids().values();
        biomassGrids.forEach(gridsPerSpecies -> {
            assertEquals(3, gridsPerSpecies.size());
            gridsPerSpecies.values().forEach(grid -> {
                assertEquals(100, grid.getHeight());
                assertEquals(101, grid.getWidth());
                assertEquals(1.0, Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum(), EPSILON);
            });
        });
    }
}