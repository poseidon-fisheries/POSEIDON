package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MirroredPyramidsAllocatorTest {


    @Test
    public void pyramids() {

        PyramidsAllocator allocator = new PyramidsAllocator(
                .2,2,1000, Lists.newArrayList(new int[]{2,2})
        );


        SeaTile middle = new SeaTile(2,2,-100,new TileHabitat(0));
        SeaTile oneOff = new SeaTile(3,2,-100,new TileHabitat(0));
        SeaTile twoOff = new SeaTile(4,2,-100,new TileHabitat(0));
        SeaTile threeOff = new SeaTile(5,2,-100,new TileHabitat(0));
        SeaTile diagonal = new SeaTile(3,3,-100,new TileHabitat(0));

        //map to fool allocator
        NauticalMap map = mock(NauticalMap.class);
        final SeaTile fakeReturn = mock(SeaTile.class); when(fakeReturn.isWater()).thenReturn(true);
        when(map.getSeaTile(anyInt(),anyInt())).thenReturn(fakeReturn);
        when(map.getWidth()).thenReturn(10);
        when(map.getHeight()).thenReturn(10);
        final MersenneTwisterFast random = new MersenneTwisterFast();

        //peak is 10000
        Assert.assertEquals(1000,
                allocator.allocate(
            middle,
                map,
                random
        ),
                .001);
        //one off: 20% left
        Assert.assertEquals(200,
                allocator.allocate(oneOff, map, random),
                .001);

        //maxSpread is 2 which means there is nothing to fish elsewhere
        Assert.assertEquals(0,
                allocator.allocate(twoOff, map, random),
                .001);
        Assert.assertEquals(0,
                allocator.allocate(threeOff, map, random),
                .001);
        //diagonal is still only 1 off (square base pyramid)
        Assert.assertEquals(200,
                allocator.allocate(diagonal, map, random),
                .001);


    }


    @Test
    public void antiPyramid() {

        BiomassAllocator allocator = new MirroredPyramidsAllocator(new PyramidsAllocator(
                .2,2,1000, Lists.newArrayList(new int[]{2,2})
        ),
                0);


        SeaTile middle = new SeaTile(2,2,-100,new TileHabitat(0));
        SeaTile oneOff = new SeaTile(3,2,-100,new TileHabitat(0));
        SeaTile twoOff = new SeaTile(4,2,-100,new TileHabitat(0));
        SeaTile threeOff = new SeaTile(5,2,-100,new TileHabitat(0));
        SeaTile diagonal = new SeaTile(3,3,-100,new TileHabitat(0));

        //map to fool allocator
        NauticalMap map = mock(NauticalMap.class);
        final SeaTile fakeReturn = mock(SeaTile.class); when(fakeReturn.isWater()).thenReturn(true);
        when(map.getSeaTile(anyInt(),anyInt())).thenReturn(fakeReturn);
        when(map.getWidth()).thenReturn(10);
        when(map.getHeight()).thenReturn(10);
        final MersenneTwisterFast random = new MersenneTwisterFast();

        //peak is 0 now
        Assert.assertEquals(0,
                allocator.allocate(
                        middle,
                        map,
                        random
                ),
                .001);
        //one off: 20% left- reversi
        Assert.assertEquals(.8,
                allocator.allocate(oneOff, map, random),
                .001);

        //maxSpread is 2 which means there the fish is elsewhere
        Assert.assertEquals(1,
                allocator.allocate(twoOff, map, random),
                .001);
        Assert.assertEquals(1,
                allocator.allocate(threeOff, map, random),
                .001);
        //diagonal is still only 1 off (square base pyramid)
        Assert.assertEquals(.8,
                allocator.allocate(diagonal, map, random),
                .001);


    }
}