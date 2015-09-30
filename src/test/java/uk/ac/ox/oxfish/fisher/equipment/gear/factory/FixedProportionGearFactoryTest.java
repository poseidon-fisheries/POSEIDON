package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FixedProportionGearFactoryTest
{


    @Test
    public void fixedProportion() throws Exception
    {


        FixedProportionGearFactory fixedProportionGearFactory =
                (FixedProportionGearFactory) Gears.CONSTRUCTORS.get(
                "Fixed Proportion").get();


        fixedProportionGearFactory.setCatchabilityPerHour(new FixedDoubleParameter(.5));
        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        FixedProportionGear gear = fixedProportionGearFactory.apply(state);
        assertEquals(gear.getProportionFished(),.5,.001);


    }
}