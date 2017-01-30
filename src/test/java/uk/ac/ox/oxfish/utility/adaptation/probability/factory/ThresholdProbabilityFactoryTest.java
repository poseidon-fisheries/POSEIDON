package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.utility.adaptation.probability.ThresholdExplorationProbability;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 1/29/17.
 */
public class ThresholdProbabilityFactoryTest {


    @Test
    public void thresholdExploration() throws Exception {

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);



        ThresholdProbabilityFactory factory = new ThresholdProbabilityFactory();
        factory.setThreshold(new FixedDoubleParameter(10d));
        ThresholdExplorationProbability ten = factory.apply(model);
        ten.start(model, fisher);
        factory.setThreshold(new FixedDoubleParameter(20d));
        ThresholdExplorationProbability twenty = factory.apply(model); twenty.start(model, fisher);
        factory.setThreshold(new FixedDoubleParameter(5d));
        ThresholdExplorationProbability five = factory.apply(model); five.start(model, fisher);

        when(model.getLatestDailyObservation(FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS)).thenReturn(10d);


        //trip returns exactly profits equal to average
        TripRecord record = mock(TripRecord.class);
        when(record.getProfitPerHour(true)).thenReturn(10d);
        ten.reactToFinishedTrip(record);
        twenty.reactToFinishedTrip(record);
        five.reactToFinishedTrip(record);
        assertEquals(1d,ten.getExplorationProbability(),.001);
        assertEquals(1d,twenty.getExplorationProbability(),.001);
        assertEquals(0d,five.getExplorationProbability(),.001);

        //very low profits: everybody explores
        when(record.getProfitPerHour(true)).thenReturn(2d);
        ten.reactToFinishedTrip(record);
        twenty.reactToFinishedTrip(record);
        five.reactToFinishedTrip(record);
        assertEquals(1d,ten.getExplorationProbability(),.001);
        assertEquals(1d,twenty.getExplorationProbability(),.001);
        assertEquals(1d,five.getExplorationProbability(),.001);

        //very high profits, nobody explores
        when(record.getProfitPerHour(true)).thenReturn(21d);
        ten.reactToFinishedTrip(record);
        twenty.reactToFinishedTrip(record);
        five.reactToFinishedTrip(record);
        assertEquals(0d,ten.getExplorationProbability(),.001);
        assertEquals(0d,twenty.getExplorationProbability(),.001);
        assertEquals(0d,five.getExplorationProbability(),.001);


    }
}