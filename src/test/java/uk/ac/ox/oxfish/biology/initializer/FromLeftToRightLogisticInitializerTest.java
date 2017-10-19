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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 2/12/16.
 */
public class FromLeftToRightLogisticInitializerTest {


    @Test
    public void leftToRightInitializer() throws Exception {


        Species species = new Species("test");
        GlobalBiology biology = new GlobalBiology(species);
        DiffusingLogisticInitializer delegate = mock(DiffusingLogisticInitializer.class);

        FromLeftToRightLogisticInitializer initializer = new FromLeftToRightLogisticInitializer(delegate,.1);

        //the leftmost cell shouldn't be bothered
        SeaTile tile = mock(SeaTile.class);
        when(tile.getGridX()).thenReturn(0);
        BiomassLocalBiology local = mock(BiomassLocalBiology.class);
        when(local.getCarryingCapacity(species)).thenReturn(100d);
        when(delegate.generateLocal(any(),any(),any(),anyInt(),anyInt(),any() )).thenReturn(local);
        initializer.generateLocal(biology, tile,new MersenneTwisterFast(),100,100,                                           mock(NauticalMap.class)
        );
        verify(local).setCarryingCapacity(species,100d);

        //in the middle you lose 50%
        tile = mock(SeaTile.class);
        when(tile.getGridX()).thenReturn(50);
        local = mock(BiomassLocalBiology.class);
        when(local.getCarryingCapacity(species)).thenReturn(100d);
        when(delegate.generateLocal(any(),any(),any(),anyInt(),anyInt(),any() )).thenReturn(local);
        initializer.generateLocal(biology, tile,new MersenneTwisterFast(),100,100,                                           mock(NauticalMap.class)
        );
        verify(local).setCarryingCapacity(species,50d);


        //rightmost you lose 10% (bound)
        tile = mock(SeaTile.class);
        when(tile.getGridX()).thenReturn(99);
        local = mock(BiomassLocalBiology.class);
        when(local.getCarryingCapacity(species)).thenReturn(100d);
        when(delegate.generateLocal(any(),any(),any(),anyInt(),anyInt(),any() )).thenReturn(local);
        initializer.generateLocal(biology, tile,new MersenneTwisterFast(),100,100,                                          mock(NauticalMap.class)
        );
        verify(local).setCarryingCapacity(species,10d);

    }
}