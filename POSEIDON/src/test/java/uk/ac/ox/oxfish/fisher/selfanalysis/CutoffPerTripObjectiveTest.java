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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripLogger;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.CutoffPerTripObjectiveFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 1/28/17.
 */
public class CutoffPerTripObjectiveTest {


    @Test
    public void correct() throws Exception {

        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        TripLogger logger = new TripLogger();
        logger.setNumberOfSpecies(1);
        logger.newTrip(0, 0, fisher);
        logger.recordCosts(100);
        logger.recordEarnings(0, 1, 100);
        logger.finishTrip(10, mock(Port.class), fisher);

        logger.newTrip(0, 0, fisher);
        logger.recordCosts(100);
        logger.recordEarnings(0, 1, 200);
        logger.finishTrip(10, mock(Port.class), fisher);
        when(fisher.getFinishedTrips()).thenReturn(logger.getFinishedTrips());
        when(fisher.getLastFinishedTrip()).thenReturn(logger.getLastFinishedTrip());

        HourlyProfitInTripObjective tripFunction = new HourlyProfitInTripObjective();
        Assertions.assertEquals(tripFunction.computeCurrentFitness(fisher, fisher), 10d, .001);


        CutoffPerTripObjectiveFactory factory = new CutoffPerTripObjectiveFactory();
        factory.setOpportunityCosts(true);
        factory.getLowThreshold().setValue(new FixedDoubleParameter(5d));
        factory.getLowThreshold().setActive(true);
        CutoffPerTripObjective objective = factory.apply(mock(FishState.class));
        Assertions.assertEquals(objective.computeCurrentFitness(fisher, fisher), 10d, .001);
        factory.getHighThreshold().setValue(new FixedDoubleParameter(7d));
        factory.getHighThreshold().setActive(true);
        objective = factory.apply(mock(FishState.class));
        Assertions.assertEquals(objective.computeCurrentFitness(fisher, fisher), 7d, .001);

    }

}
