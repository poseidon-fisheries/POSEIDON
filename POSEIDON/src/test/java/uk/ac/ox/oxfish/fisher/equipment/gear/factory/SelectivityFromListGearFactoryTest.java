package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticSimpleFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SelectivityFromListGearFactoryTest {

    @Test
    public void selectFromListCorrectly() {


        Species species = mock(Species.class);
        when(species.getNumberOfSubdivisions()).thenReturn(1);
        when(species.getNumberOfBins()).thenReturn(3);

        SelectivityFromListGearFactory factory = new SelectivityFromListGearFactory();
        factory.setSelectivityPerBin("0d,1d,.5d");
        factory.setAverageCatchability(new FixedDoubleParameter(.5d));

        final HomogeneousAbundanceGear gear = factory.apply(mock(FishState.class));
        final double[][] selected = gear.filter(
                species,
                new double[][]{{100, 100, 100}}
        );


        assertEquals(selected.length,1);
        //1/(1+exp(15.0948823-0.5391899*(c(25,40,100))))
        // 0.1658769 0.9984574 1.0000000
        assertEquals(selected[0][0],0,.001);
        assertEquals(selected[0][1],50,.001);
        assertEquals(selected[0][2],25,.001);

    }
}