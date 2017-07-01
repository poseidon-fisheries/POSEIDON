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