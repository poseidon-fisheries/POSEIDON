package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/11/17.
 */
public class DepthAllocatorTest {


    @Test
    public void depthAllocator() throws Exception {


        DepthAllocator allocator = new DepthAllocator(
                0,0,5,10,
                105,300
        );


        SeaTile tile = mock(SeaTile.class);
        when(tile.getGridX()).thenReturn(0);
        when(tile.getGridY()).thenReturn(0);

        when(tile.getAltitude()).thenReturn(-100d);
        double allocate = allocator.allocate(
                tile,
                mock(NauticalMap.class),
                new MersenneTwisterFast()
        );
        assertEquals(0d,allocate,.001);


        when(tile.getAltitude()).thenReturn(-105d);
        allocate = allocator.allocate(
                tile,
                mock(NauticalMap.class),
                new MersenneTwisterFast()
        );
        assertEquals(1d,allocate,.001);


        when(tile.getAltitude()).thenReturn(-500d);
        allocate = allocator.allocate(
                tile,
                mock(NauticalMap.class),
                new MersenneTwisterFast()
        );
        assertEquals(0d,allocate,.001);

        when(tile.getAltitude()).thenReturn(-300d);
        allocate = allocator.allocate(
                tile,
                mock(NauticalMap.class),
                new MersenneTwisterFast()
        );
        assertEquals(1d,allocate,.001);

        when(tile.getGridY()).thenReturn(10);
        allocate = allocator.allocate(
                tile,
                mock(NauticalMap.class),
                new MersenneTwisterFast()
        );
        assertEquals(1d,allocate,.001);


        when(tile.getGridY()).thenReturn(11);
        allocate = allocator.allocate(
                tile,
                mock(NauticalMap.class),
                new MersenneTwisterFast()
        );
        assertEquals(0d,allocate,.001);

    }
}