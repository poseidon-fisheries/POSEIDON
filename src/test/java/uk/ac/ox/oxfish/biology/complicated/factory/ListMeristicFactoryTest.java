package uk.ac.ox.oxfish.biology.complicated.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 7/7/17.
 */
public class ListMeristicFactoryTest {


    @Test
    public void buildsAllright() throws Exception {

        ListMeristicFactory factory = new ListMeristicFactory();
        factory.setMortalityRate(new FixedDoubleParameter(.2));
        factory.setWeightsPerBin("1,2,3,4,5,6");
        FromListMeristics meristics = factory.apply(mock(FishState.class));

        assertEquals(meristics.getMaxAge(),5);
        assertEquals(meristics.getWeightMaleInKg().get(2),3,.001);



    }
}