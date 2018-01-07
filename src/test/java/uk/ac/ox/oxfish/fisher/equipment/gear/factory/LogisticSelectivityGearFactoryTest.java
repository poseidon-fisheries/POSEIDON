package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.SelectivityAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

public class LogisticSelectivityGearFactoryTest {


    @Test
    public void create()
    {

        LogisticSelectivityGearFactory factory = new LogisticSelectivityGearFactory();
        factory.setAverageCatchability(new FixedDoubleParameter(.123));
        factory.setSelectivityAParameter(new FixedDoubleParameter(.234));
        factory.setSelectivityBParameter(new FixedDoubleParameter(.345));


        SelectivityAbundanceGear gear = (SelectivityAbundanceGear) factory.apply(new FishState());
        assertEquals(gear.getaParameter(),.234,.0001);
        assertEquals(gear.getbParameter(),.345,.0001);

    }
}