/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.biology.initializer.allocator.BoundedAllocatorDecorator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/11/17.
 */
public class DepthAllocatorDecoratorTest {


    @Test
    public void depthAllocator() throws Exception {


        DepthAllocatorDecorator allocator = new DepthAllocatorDecorator(
             
                105,300,
                new BoundedAllocatorDecorator(0,0,5,10,true,
                    new ConstantBiomassAllocator())
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
