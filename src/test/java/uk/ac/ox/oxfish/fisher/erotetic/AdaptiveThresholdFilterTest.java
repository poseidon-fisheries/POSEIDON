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
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/2/16.
 */
public class AdaptiveThresholdFilterTest {

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
        Function<FishState,Double> adaptor = mock(Function.class);
        when(adaptor.apply(any())).thenReturn(1d,2d,3d);
        AdaptiveThresholdFilter<Double> filter =
                new AdaptiveThresholdFilter<>(
                        3,
                        0,
                        "feature",
                        adaptor,
                        1
                );



        List<Double> selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)

        );
        assertEquals(selected.size(),3);
        filter.step(mock(FishState.class));

        selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)
        );
        assertEquals(selected.size(),3);
        filter.step(mock(FishState.class));

        selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)
        );
        assertEquals(selected.size(),2);
        filter.step(mock(FishState.class));

        selected = filter.filterOptions(
                options,
                extractor,
                mock(FishState.class),
                mock(Fisher.class)
        );
        assertEquals(selected.size(),1);
        filter.step(mock(FishState.class));
    }
}