package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.Lists;
import org.junit.Assert;
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
    public void stayVirgin() throws Exception {

        //numbers are from sablefish

        LogisticLocalBiology biology =
                new LogisticLocalBiology(new Double[]{527154d},
                                         new Double[]{527154d});

        ArrayList<Double> biomasses = Lists.newArrayList(527154d, 527154d, 527154d, 527154d, 527154d, 527154d);
        DerisoSchnuteIndependentGrower grower =
                new DerisoSchnuteIndependentGrower(
                        biomasses, 1.03, 0.92311,
                        0.6, 3, 0,
                        1.03313,
                        1.01604,
                        527154d,
                        29728.8

                );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 20; i++) {
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }
        Assert.assertEquals(biology.getCurrentBiomass()[0],527154d,.001);

    }

    @Test
    public void fivePercentFishingFromVirginTest() throws Exception {

        //numbers are from sablefish

        LogisticLocalBiology biology =
                new LogisticLocalBiology(new Double[]{527154d},
                                         new Double[]{527154d});

        ArrayList<Double> biomasses = Lists.newArrayList(527154d, 527154d, 527154d, 527154d, 527154d, 527154d);
        DerisoSchnuteIndependentGrower grower =
                new DerisoSchnuteIndependentGrower(
                        biomasses, 1.03, 0.92311,
                        0.6, 3, 0,
                        1.03313,
                        1.01604,
                        527154d,
                        29728.8

                );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 20; i++) {
            biology.getCurrentBiomass()[0] =  biology.getCurrentBiomass()[0]*.95; //5% mortality!
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }

        Assert.assertEquals(297073.2,biology.getCurrentBiomass()[0],.0001);
    }

    @Test
    public void fivePercentFishingFromVirginTestYelloweye() throws Exception {

        //numbers are from sablefish

        LogisticLocalBiology biology =
                new LogisticLocalBiology(new Double[]{8883d},
                                         new Double[]{8883d});

        ArrayList<Double> biomasses = Lists.newArrayList(8883d, 8883d, 8883d, 8883d, 8883d,
                                                         8883d,8883d,8883d,8883d,8883d,
                                                         8883d,8883d,8883d,8883d,8883d,
                                                         8883d,8883d,8883d,8883d,8883d,
                                                         8883d,8883d);
        DerisoSchnuteIndependentGrower grower =
                new DerisoSchnuteIndependentGrower(
                        biomasses, 0.922, 0.95504,
                        0.44056, 14, 0,
                        1.11910,
                        .63456,
                        8883d,
                        85.13962

                );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 20; i++) {
            biology.getCurrentBiomass()[0] =  biology.getCurrentBiomass()[0]*.95; //5% mortality!
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }

        Assert.assertEquals(4184.71,biology.getCurrentBiomass()[0],.0001);
    }

    /*
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
    */
}