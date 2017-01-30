package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class TwoSpeciesBoxInitializerTest
{


    @Test
    public void killsOffCorrectly() throws Exception {

        //simple box 0,0 to 9,9
        TwoSpeciesBoxInitializer initializer = new TwoSpeciesBoxInitializer(
                0,
                0,
                10,
                10,
                false,
                new FixedDoubleParameter(100),
                new FixedDoubleParameter(1d),
                0d,0d,
                new SimpleLogisticGrowerInitializer(new FixedDoubleParameter(1d))
        );


        GlobalBiology biology = new GlobalBiology(new Species("A"), new Species("B"));
        //at 0,0 there is no species 0
        LogisticLocalBiology zerozero = (LogisticLocalBiology)
                initializer.generateLocal(biology,
                                          new SeaTile(0, 0, -100, mock(TileHabitat.class)),
                                          new MersenneTwisterFast(),
                                          100,
                                          100
                );

        assertEquals(zerozero.getCarryingCapacity(biology.getSpecie(0)),0,.0001 );
        assertEquals(zerozero.getCarryingCapacity(biology.getSpecie(1)),100,.0001 );

        //at 5,5 also no species 0
        LogisticLocalBiology fivefive = (LogisticLocalBiology)
                initializer.generateLocal(biology,
                                          new SeaTile(5,5, -100, mock(TileHabitat.class)),
                                          new MersenneTwisterFast(),
                                          100,
                                          100
                );
        assertEquals(fivefive.getCarryingCapacity(biology.getSpecie(0)),0,.0001 );
        assertEquals(fivefive.getCarryingCapacity(biology.getSpecie(1)),100,.0001 );


        //at 10,10 there is no species 1
        LogisticLocalBiology tenten = (LogisticLocalBiology)
                initializer.generateLocal(biology,
                                          new SeaTile(10,10, -100, mock(TileHabitat.class)),
                                          new MersenneTwisterFast(),
                                          100,
                                          100
                );
        assertEquals(tenten.getCarryingCapacity(biology.getSpecie(0)),100,.0001 );
        assertEquals(tenten.getCarryingCapacity(biology.getSpecie(1)),0,.0001 );
    }
}