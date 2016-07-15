package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/29/16.
 */
public class HeatmapDestinationStrategyTest {


    @Test
    public void addsObservationsWithImitation() throws Exception {


        GeographicalRegression regression = mock(GeographicalRegression.class);
        Fisher user = mock(Fisher.class);
        Fisher friend = mock(Fisher.class);
        when(user.getDirectedFriends()).thenReturn(Lists.newArrayList(friend));

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        AcquisitionFunction optimizer = mock(AcquisitionFunction.class);

        HeatmapDestinationStrategy strategy = new HeatmapDestinationStrategy(
                regression,
                optimizer,
                false,
                new FixedProbability(0d,1d),
                model.getMap(),
                model.getRandom(),
                10
        );
        SeaTile optimal = mock(SeaTile.class);

        when(optimal.getAltitude()).thenReturn(-100d);
        when(optimizer.pick(any(), any(), any(),any(),any() )).thenReturn(optimal);

        strategy.start(model,user);

        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class,RETURNS_DEEP_STUBS));

        //should add two observations!
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        verify(regression,times(2)).addObservation(any(),any() );

        //one only now because we already added our friend's
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        verify(regression,times(3)).addObservation(any(),any() );

        //and now two more
        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        verify(regression,times(5)).addObservation(any(),any() );

    }

    @Test
    public void addsObservationsWithoutImitation() throws Exception {


        GeographicalRegression regression = mock(GeographicalRegression.class);
        Fisher user = mock(Fisher.class);
        Fisher friend = mock(Fisher.class);
        when(user.getDirectedFriends()).thenReturn(Lists.newArrayList(friend));
        AcquisitionFunction optimizer = mock(AcquisitionFunction.class);

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        HeatmapDestinationStrategy strategy = new HeatmapDestinationStrategy(
                regression,
                optimizer,
                false,
                new FixedProbability(0d,0d),
                model.getMap(),
                model.getRandom(),
                10
        );
        SeaTile optimal = mock(SeaTile.class);

        when(optimal.getAltitude()).thenReturn(-100d);
        when(optimizer.pick(any(), any(), any(),any(),any() )).thenReturn(optimal);

        strategy.start(model,user);

        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class,RETURNS_DEEP_STUBS));

        //should add one observations (ignore friend)
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        verify(regression,times(1)).addObservation(any(),any() );

        //one only now because we already checked our friend's latest
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        verify(regression,times(2)).addObservation(any(),any() );

        //and now ignore your friend again
        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        verify(regression,times(3)).addObservation(any(),any() );

    }

    @Test
    public void acquisitionWorks() throws Exception {

        Fisher user = mock(Fisher.class);

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        AcquisitionFunction optimizer = mock(AcquisitionFunction.class);

        HeatmapDestinationStrategy strategy =  new HeatmapDestinationStrategy(
                mock(GeographicalRegression.class),
                optimizer,
                false,
                new FixedProbability(0d,1d),
                model.getMap(),
                model.getRandom(),
                10
        );


        strategy.start(model,user);


        SeaTile optimal = mock(SeaTile.class);
        assertNotEquals(strategy.getFavoriteSpot(),
                        optimal);
        when(optimal.getAltitude()).thenReturn(-100d);
        when(optimizer.pick(any(), any(), any(),any(),any() )).thenReturn(optimal);
        strategy.reactToFinishedTrip(mock(TripRecord.class,RETURNS_DEEP_STUBS));
        assertEquals(strategy.getFavoriteSpot(),
                     optimal);





    }
}