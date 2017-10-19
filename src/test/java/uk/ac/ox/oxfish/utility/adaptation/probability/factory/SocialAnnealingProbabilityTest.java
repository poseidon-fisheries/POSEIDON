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
public class SocialAnnealingProbabilityTest {

    @Test
    public void socialAnnealing() throws Exception {

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        Fisher fisher = mock(Fisher.class,RETURNS_DEEP_STUBS);



        SocialAnnealingProbabilityFactory factory = new SocialAnnealingProbabilityFactory();
        factory.setMultiplier(new FixedDoubleParameter(1d));
        ThresholdExplorationProbability annealing = factory.apply(model);
        annealing.start(model, fisher);
        factory.setMultiplier(new FixedDoubleParameter(2d));
        ThresholdExplorationProbability annealing2 = factory.apply(model); annealing2.start(model, fisher);
        factory.setMultiplier(new FixedDoubleParameter(0.5d));
        ThresholdExplorationProbability annealing05 = factory.apply(model); annealing05.start(model, fisher);

        when(model.getLatestDailyObservation(FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS)).thenReturn(10d);


        //trip returns exactly profits equal to average
        TripRecord record = mock(TripRecord.class);
        when(record.getProfitPerHour(true)).thenReturn(10d);
        annealing.reactToFinishedTrip(record);
        annealing2.reactToFinishedTrip(record);
        annealing05.reactToFinishedTrip(record);
        assertEquals(1d,annealing.getExplorationProbability(),.001);
        assertEquals(1d,annealing2.getExplorationProbability(),.001);
        assertEquals(0d,annealing05.getExplorationProbability(),.001);

        //very low profits: everybody explores
        when(record.getProfitPerHour(true)).thenReturn(2d);
        annealing.reactToFinishedTrip(record);
        annealing2.reactToFinishedTrip(record);
        annealing05.reactToFinishedTrip(record);
        assertEquals(1d,annealing.getExplorationProbability(),.001);
        assertEquals(1d,annealing2.getExplorationProbability(),.001);
        assertEquals(1d,annealing05.getExplorationProbability(),.001);

        //very high profits, nobody explores
        when(record.getProfitPerHour(true)).thenReturn(21d);
        annealing.reactToFinishedTrip(record);
        annealing2.reactToFinishedTrip(record);
        annealing05.reactToFinishedTrip(record);
        assertEquals(0d,annealing.getExplorationProbability(),.001);
        assertEquals(0d,annealing2.getExplorationProbability(),.001);
        assertEquals(0d,annealing05.getExplorationProbability(),.001);


    }
}