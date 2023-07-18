/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/29/16.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class HeatmapDestinationStrategyTest {


    @Test
    public void addsObservationsWithImitation() throws Exception {


        final GeographicalRegression regression = mock(GeographicalRegression.class);
        final Fisher user = mock(Fisher.class);
        final Fisher friend = mock(Fisher.class);
        when(user.getDirectedFriends()).thenReturn(Lists.newArrayList(friend));

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final AcquisitionFunction optimizer = mock(AcquisitionFunction.class);

        final HeatmapDestinationStrategy strategy = new HeatmapDestinationStrategy(
            regression,
            optimizer,
            false,
            new FixedProbability(0d, 1d),
            model.getMap(),
            model.getRandom(),
            10,
            new HourlyProfitInTripObjective(true)
        );
        final SeaTile optimal = mock(SeaTile.class);

        when(optimal.isWater()).thenReturn(true);
        when(optimizer.pick(any(), any(), any(), any(), any())).thenReturn(optimal);

        strategy.start(model, user);

        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class, RETURNS_DEEP_STUBS));

        //should add two observations!
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        verify(regression, times(2)).addObservation(any(), any(), any());

        //one only now because we already added our friend's
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        verify(regression, times(3)).addObservation(any(), any(), any());

        //and now two more
        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class, RETURNS_DEEP_STUBS));
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        verify(regression, times(5)).addObservation(any(), any(), any());

    }

    @Test
    public void addsObservationsWithoutImitation() throws Exception {


        final GeographicalRegression regression = mock(GeographicalRegression.class);
        final Fisher user = mock(Fisher.class);
        final Fisher friend = mock(Fisher.class);
        when(user.getDirectedFriends()).thenReturn(Lists.newArrayList(friend));
        final AcquisitionFunction optimizer = mock(AcquisitionFunction.class);

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        final HeatmapDestinationStrategy strategy = new HeatmapDestinationStrategy(
            regression,
            optimizer,
            false,
            new FixedProbability(0d, 0d),
            model.getMap(),
            model.getRandom(),
            10,
            new HourlyProfitInTripObjective(true)
        );
        final SeaTile optimal = mock(SeaTile.class);

        when(optimal.isWater()).thenReturn(true);
        when(optimizer.pick(any(), any(), any(), any(), any())).thenReturn(optimal);

        strategy.start(model, user);

        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class, RETURNS_DEEP_STUBS));

        //should add one observations (ignore friend)
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        verify(regression, times(1)).addObservation(any(), any(), any());

        //one only now because we already checked our friend's latest
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        verify(regression, times(2)).addObservation(any(), any(), any());

        //and now ignore your friend again
        when(friend.getLastFinishedTrip()).thenReturn(mock(TripRecord.class, RETURNS_DEEP_STUBS));
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        verify(regression, times(3)).addObservation(any(), any(), any());

    }

    @Test
    public void acquisitionWorks() throws Exception {

        final Fisher user = mock(Fisher.class);

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        final AcquisitionFunction optimizer = mock(AcquisitionFunction.class);

        final HeatmapDestinationStrategy strategy = new HeatmapDestinationStrategy(
            mock(GeographicalRegression.class),
            optimizer,
            false,
            new FixedProbability(0d, 1d),
            model.getMap(),
            model.getRandom(),
            10,
            new HourlyProfitInTripObjective(true)
        );


        strategy.start(model, user);


        final SeaTile optimal = mock(SeaTile.class);
        Assertions.assertNotEquals(strategy.getFavoriteSpot(), optimal);
        when(optimal.isWater()).thenReturn(true);
        when(optimizer.pick(any(), any(), any(), any(), any())).thenReturn(optimal);
        strategy.reactToFinishedTrip(
            mock(TripRecord.class, RETURNS_DEEP_STUBS),
            mock(Fisher.class, RETURNS_DEEP_STUBS)
        );
        Assertions.assertEquals(strategy.getFavoriteSpot(), optimal);


    }
}