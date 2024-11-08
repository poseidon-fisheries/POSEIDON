package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.SelectivityAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class LogisticSelectivityGearFactoryTest {


    @Test
    public void create() {

        LogisticSelectivityGearFactory factory = new LogisticSelectivityGearFactory();
        factory.setAverageCatchability(new FixedDoubleParameter(.123));
        factory.setSelectivityAParameter(new FixedDoubleParameter(.234));
        factory.setSelectivityBParameter(new FixedDoubleParameter(.345));


        SelectivityAbundanceGear gear = (SelectivityAbundanceGear) factory.apply(new FishState());
        Assertions.assertEquals(gear.getaParameter(), .234, .0001);
        Assertions.assertEquals(gear.getbParameter(), .345, .0001);

    }
}
