package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/19/17.
 */
public class LogitReturnStrategyTest {


    @Test
    public void shouldIReturn() throws Exception {

        //empty fisher
        Fisher fisher = mock(Fisher.class);
        when(fisher.getMaximumHold()).thenReturn(100d);


        //always wants to return!
        LogisticClassifier classifier = mock(LogisticClassifier.class);

        LogitReturnStrategy strategy =
                new LogitReturnStrategy(classifier);



        //true gets propagated
        when(classifier.test(any(),any(),any(),any())).thenReturn(true);

        //always true because having put no fishing effort overrides the logit
        TripRecord record = mock(TripRecord.class);
        when(record.getEffort()).thenReturn(0);
        assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));
        assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));
        assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));
        assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));

        //when effort is at least 1, shouldFish returns the opposite of the classifier
        when(record.getEffort()).thenReturn(1);
        when(classifier.test(any(),any(),any(),any())).thenReturn(true);
        assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                        record));
        assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                        record));
        when(classifier.test(any(),any(),any(),any())).thenReturn(false);
        assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));
        assertTrue(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));

        //you should return if when the classifier says not to if you are full
        when(classifier.test(any(),any(),any(),any())).thenReturn(false);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        assertFalse(strategy.shouldFish(fisher, new MersenneTwisterFast(), mock(FishState.class),
                                       record));
    }
}