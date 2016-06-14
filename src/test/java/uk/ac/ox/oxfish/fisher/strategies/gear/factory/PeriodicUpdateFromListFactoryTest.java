package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 6/14/16.
 */
public class PeriodicUpdateFromListFactoryTest {


    @Test
    public void readFromYamlCorrectly() throws Exception {

        String toRead = "Periodic Gear Update from List:\n" +
                "  availableGears:\n" +
                "    - Fixed Proportion:\n" +
                "        catchabilityPerHour: '0.01'\n" +
                "    - Fixed Proportion:\n" +
                "        catchabilityPerHour: '0.02'\n" +
                "  probability:\n" +
                "    Fixed Probability:\n" +
                "      explorationProbability: '0.2'\n" +
                "      imitationProbability: '0.6'\n" +
                "  yearly: true";

        FishYAML yamler = new FishYAML();
        AlgorithmFactory<? extends GearStrategy> gearStrategy = yamler.loadAs(toRead, AlgorithmFactory.class);

        assertTrue(gearStrategy.getClass().equals(PeriodicUpdateFromListFactory.class));
        PeriodicUpdateFromListFactory casted = (PeriodicUpdateFromListFactory) gearStrategy;
        assertEquals(2,casted.getAvailableGears().size());
        for(AlgorithmFactory<? extends Gear> gearFactory : casted.getAvailableGears())
        {
            assertTrue(gearFactory.getClass().equals(FixedProportionGearFactory.class));
            DoubleParameter catchabilityPerHour = ((FixedProportionGearFactory) gearFactory).getCatchabilityPerHour();
            assertTrue(((FixedDoubleParameter) catchabilityPerHour).getFixedValue()==0.01 ||
                               ((FixedDoubleParameter) catchabilityPerHour).getFixedValue()==0.02);
        }




    }
}