package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 2/6/17.
 */
public class LinearGetterBiologyInitializerTest {


    @Test
    public void simpleFunction() throws Exception {

        FishState state = mock(FishState.class);
        //0,0 with 0% rocky
        SeaTile zeroZero =  new SeaTile(0,0,-100,new TileHabitat(0));
        //5,5 with 50% rocky
        SeaTile fiveFive = new SeaTile(5,5,-100,new TileHabitat(.5));

        LinearGetterBiologyInitializer initializer = new LinearGetterBiologyInitializer(
                100,
                1,
                2,
                -1,
                0,
                10,
                0,
                0,
                0
        );


        GlobalBiology globalBiology = initializer.generateGlobal(new MersenneTwisterFast(), state);
        zeroZero.setBiology(initializer.generateLocal(globalBiology,
                                                                 zeroZero,
                                                                 new MersenneTwisterFast(),
                                                                 5,5,                                          mock(NauticalMap.class)
        ));
        fiveFive.setBiology(initializer.generateLocal(globalBiology,
                                                      fiveFive,
                                                      new MersenneTwisterFast(),
                                                      5,5,                                          mock(NauticalMap.class)
        ));

        zeroZero.start(state);
        fiveFive.start(state);

        when(state.getDayOfTheYear()).thenReturn(0);
        assertEquals(zeroZero.getBiomass(globalBiology.getSpecie(0)),100d,0.001d);
        //100 + 5 + 10 + 10*.5
        assertEquals(fiveFive.getBiomass(globalBiology.getSpecie(0)),120d,0.001d);


        when(state.getDayOfTheYear()).thenReturn(2);
        assertEquals(zeroZero.getBiomass(globalBiology.getSpecie(0)),100d,0.001d);
        //100 + 5 + 10 + 10*.5 - 10
        assertEquals(fiveFive.getBiomass(globalBiology.getSpecie(0)),110d,0.001d);

    }
}