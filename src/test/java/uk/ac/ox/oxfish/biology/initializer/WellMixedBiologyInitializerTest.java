package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class WellMixedBiologyInitializerTest {


    @Test
    public void initializerMixesWell() throws Exception {

        WellMixedBiologyInitializer initializer =
                new WellMixedBiologyInitializer(new FixedDoubleParameter(60),
                                                new FixedDoubleParameter(.25),
                                                .01,.01,
                                                new SimpleLogisticGrowerInitializer(new FixedDoubleParameter(.8d))
                );



        SeaTile tile = mock(SeaTile.class);
        BiomassLocalBiology biology = (BiomassLocalBiology) initializer.generateLocal(mock(GlobalBiology.class),
                                                                                      tile,
                                                                                      new MersenneTwisterFast(), 50,
                                                                                      50, );


        Species zero = new Species("zero"); zero.resetIndexTo(0);
        Species one = new Species("one"); one.resetIndexTo(1);


        assertEquals(biology.getCarryingCapacity(zero),60,.001);
        assertEquals(biology.getCarryingCapacity(one),20,.001);

    }
}