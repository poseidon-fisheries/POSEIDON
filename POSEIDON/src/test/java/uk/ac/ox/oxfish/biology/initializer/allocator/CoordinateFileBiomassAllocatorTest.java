package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory.DEFAULT_MAP_PADDING_IN_DEGREES;

public class CoordinateFileBiomassAllocatorTest {

    @Test
    public void coordinateFileBiomass() {
        //this example is from FromFileMapInitializerTest

        Path path = Paths.get("inputs", "tests", "map.csv");
        final FromFileMapInitializer initializer = new FromFileMapInitializer(
            path, 2, DEFAULT_MAP_PADDING_IN_DEGREES, true, false
        );
        final NauticalMap map = initializer.makeMap(
            new MersenneTwisterFast(),
            new GlobalBiology(mock(Species.class)),
            mock(FishState.class)
        );
        Assertions.assertEquals(map.getHeight(), 2);
        Assertions.assertEquals(map.getWidth(), 2);

        //let's just read the depth flipped upside down for abundance
        path = Paths.get("inputs", "tests", "flipped_map.csv");
        final CoordinateFileBiomassAllocator allocator = new CoordinateFileBiomassAllocator(
            path, true
        );
        Assertions.assertEquals(6, allocator.allocate(map.getSeaTile(0, 0), map, new MersenneTwisterFast()), .0001);
        Assertions.assertEquals(6, allocator.allocate(map.getSeaTile(0, 1), map, new MersenneTwisterFast()), .0001);
        //this ought to be neutralized by the initializer
        Assertions.assertEquals(-10, allocator.allocate(map.getSeaTile(1, 1), map, new MersenneTwisterFast()), .0001);
        Assertions.assertEquals(-10, allocator.allocate(map.getSeaTile(1, 1), map, new MersenneTwisterFast()), .0001);


    }

}