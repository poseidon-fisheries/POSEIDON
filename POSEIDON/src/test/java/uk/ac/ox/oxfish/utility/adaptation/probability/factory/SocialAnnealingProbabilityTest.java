/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.utility.adaptation.probability.ThresholdExplorationProbability;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 1/29/17.
 */
public class SocialAnnealingProbabilityTest {

    @Test
    public void socialAnnealing() throws Exception {

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);


        final SocialAnnealingProbabilityFactory factory = new SocialAnnealingProbabilityFactory();
        factory.setMultiplier(new FixedDoubleParameter(1d));
        final ThresholdExplorationProbability annealing = factory.apply(model);
        annealing.start(model, fisher);
        factory.setMultiplier(new FixedDoubleParameter(2d));
        final ThresholdExplorationProbability annealing2 = factory.apply(model);
        annealing2.start(model, fisher);
        factory.setMultiplier(new FixedDoubleParameter(0.5d));
        final ThresholdExplorationProbability annealing05 = factory.apply(model);
        annealing05.start(model, fisher);

        when(model.getLatestDailyObservation(FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS)).thenReturn(10d);


        //trip returns exactly profits equal to average
        final TripRecord record = mock(TripRecord.class);
        when(record.getProfitPerHour(true)).thenReturn(10d);
        annealing.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        annealing2.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        annealing05.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        Assertions.assertEquals(1d, annealing.getExplorationProbability(), .001);
        Assertions.assertEquals(1d, annealing2.getExplorationProbability(), .001);
        Assertions.assertEquals(0d, annealing05.getExplorationProbability(), .001);

        //very low profits: everybody explores
        when(record.getProfitPerHour(true)).thenReturn(2d);
        annealing.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        annealing2.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        annealing05.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        Assertions.assertEquals(1d, annealing.getExplorationProbability(), .001);
        Assertions.assertEquals(1d, annealing2.getExplorationProbability(), .001);
        Assertions.assertEquals(1d, annealing05.getExplorationProbability(), .001);

        //very high profits, nobody explores
        when(record.getProfitPerHour(true)).thenReturn(21d);
        annealing.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        annealing2.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        annealing05.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));
        Assertions.assertEquals(0d, annealing.getExplorationProbability(), .001);
        Assertions.assertEquals(0d, annealing2.getExplorationProbability(), .001);
        Assertions.assertEquals(0d, annealing05.getExplorationProbability(), .001);


    }
}
