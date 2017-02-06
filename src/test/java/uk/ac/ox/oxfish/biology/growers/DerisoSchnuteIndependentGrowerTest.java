package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 2/1/17.
 */
public class DerisoSchnuteIndependentGrowerTest {


    @Test
    public void noFishingTest() throws Exception {

        //numbers are from sablefish

        LogisticLocalBiology biology =
                new LogisticLocalBiology(new Double[]{205662d},
                                         new Double[]{527154d});

        ArrayList<Double> data = Lists.newArrayList(263494d, 248481d, 233025d, 222936d, 211793d, 205662d);
        DerisoSchnuteIndependentGrower grower =
                new DerisoSchnuteIndependentGrower(
                        data, 0.92267402483245d, 0.923116346386636,
                        0.6, 3, 0,
                        1.03312585773941,
                        0.634560212266768
                );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 100; i++) {
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }

    }
}