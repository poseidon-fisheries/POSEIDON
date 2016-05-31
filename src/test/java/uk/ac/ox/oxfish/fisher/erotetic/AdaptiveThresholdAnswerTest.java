package uk.ac.ox.oxfish.fisher.erotetic;

import org.jfree.util.Log;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/2/16.
 */
public class AdaptiveThresholdAnswerTest {

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
                    Collection<Double> toRepresent, FishState model, Fisher fisher) {
                HashMap<Double,Double> toReturn = new HashMap<>();
                for(Double number : toRepresent)
                    toReturn.put(number,number);
                return toReturn;
            }
        });
    }


    @Test
    public void adapt() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        FeatureExtractor<Double> adaptor = mock(FeatureExtractor.class);
        extractor.addFeatureExtractor("threshold",
                                      adaptor);

        HashMap<Double,Double> adaptorAnswer = mock(HashMap.class);
        when(adaptor.extractFeature(anyCollection(),
                                      any(), any())).thenReturn(adaptorAnswer);

        FeatureThresholdAnswer<Double> filter = new FeatureThresholdAnswer<>(
                3,
                "feature",
                "threshold"
        );


        when(adaptorAnswer.get(any())).thenReturn(0d);

        List<Double> selected = filter.answer(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)

        );
        assertEquals(selected.size(),3);

        when(adaptorAnswer.get(any())).thenReturn(1d);
        selected = filter.answer(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)
        );
        assertEquals(selected.size(),3);

        when(adaptorAnswer.get(any())).thenReturn(2d);
        selected = filter.answer(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)
        );
        assertEquals(selected.size(),2);

        when(adaptorAnswer.get(any())).thenReturn(3d);
        selected = filter.answer(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)
        );
        assertEquals(selected.size(),1);
    }
}