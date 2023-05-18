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

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);


        final ThresholdProbabilityFactory factory = new ThresholdProbabilityFactory();
        factory.setThreshold(new FixedDoubleParameter(10d));
        final ThresholdExplorationProbability ten = factory.apply(model);
        ten.start(model, fisher);
        factory.setThreshold(new FixedDoubleParameter(20d));
        final ThresholdExplorationProbability twenty = factory.apply(model);
        twenty.start(model, fisher);
        factory.setThreshold(new FixedDoubleParameter(5d));
        final ThresholdExplorationProbability five = factory.apply(model);
        five.start(model, fisher);

        when(model.getLatestDailyObservation(FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS)).thenReturn(10d);


        //trip returns exactly profits equal to average
        final TripRecord record = mock(TripRecord.class);
        when(record.getProfitPerHour(true)).thenReturn(10d);
        ten.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        twenty.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        five.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        assertEquals(1d, ten.getExplorationProbability(), .001);
        assertEquals(1d, twenty.getExplorationProbability(), .001);
        assertEquals(0d, five.getExplorationProbability(), .001);

        //very low profits: everybody explores
        when(record.getProfitPerHour(true)).thenReturn(2d);
        ten.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        twenty.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        five.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        assertEquals(1d, ten.getExplorationProbability(), .001);
        assertEquals(1d, twenty.getExplorationProbability(), .001);
        assertEquals(1d, five.getExplorationProbability(), .001);

        //very high profits, nobody explores
        when(record.getProfitPerHour(true)).thenReturn(21d);
        ten.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        twenty.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        five.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        assertEquals(0d, ten.getExplorationProbability(), .001);
        assertEquals(0d, twenty.getExplorationProbability(), .001);
        assertEquals(0d, five.getExplorationProbability(), .001);


    }
}