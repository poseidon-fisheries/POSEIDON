package uk.ac.ox.oxfish.fisher.erotetic;

import org.jfree.util.Log;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 5/2/16.
 */
public class ThresholdFilterTest {


    private LinkedList<Double> options;
    private FeatureExtractors<Double> extractor;

    @Before
    public void setUp() throws Exception {
        options = new LinkedList<>();
        options.add(1d);
        options.add(2d);
        options.add(3d);
        extractor = new FeatureExtractors<>();
        extractor.addFeatureExtractor("feature", new FeatureExtractor<Double>() {
            @Override
            public HashMap<Double, Double> extractFeature(
                    Collection<Double> toRepresent, FishState model, FisherEquipment equipment, FisherStatus status,
                    FisherMemory memory) {
                HashMap<Double,Double> toReturn = new HashMap<>();
                for(Double number : toRepresent)
                    toReturn.put(number,number);
                return toReturn;
            }
        });
    }

    @Test
    public void tooLow() throws Exception {


        Log.info("needs numbers to be above 10, will not select any");
        ThresholdFilter<Double> filter = new ThresholdFilter<>(
                2,
                10,
                "feature"
        );


        List<Double> selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(FisherEquipment.class),
                mock(FisherStatus.class),
                mock(FisherMemory.class)
        );

        assertTrue(selected == null || selected.isEmpty());

    }


    @Test
    public void tooFew() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        ThresholdFilter<Double> filter = new ThresholdFilter<>(
                4,
                0,
                "feature"
        );


        List<Double> selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(FisherEquipment.class),
                mock(FisherStatus.class),
                mock(FisherMemory.class)
        );

        assertTrue(selected == null || selected.isEmpty());

    }


    @Test
    public void allGood() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        ThresholdFilter<Double> filter = new ThresholdFilter<>(
                3,
                0,
                "feature"
        );


        List<Double> selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(FisherEquipment.class),
                mock(FisherStatus.class),
                mock(FisherMemory.class)
        );

        assertEquals(selected.size(),3);

    }

    @Test
    public void oneGood() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        ThresholdFilter<Double> filter = new ThresholdFilter<>(
                3,
                3,
                "feature"
        );


        List<Double> selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(FisherEquipment.class),
                mock(FisherStatus.class),
                mock(FisherMemory.class)
        );

        assertEquals(selected.size(),1);

    }
}