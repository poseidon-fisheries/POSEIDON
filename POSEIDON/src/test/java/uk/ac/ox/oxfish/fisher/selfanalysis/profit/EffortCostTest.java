/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EffortCostTest {


    @Test
    public void costCorrect() {

        final TripRecord record = mock(TripRecord.class);
        when(record.getEffort()).thenReturn(5);
        EffortCost cost = new EffortCost(12);
        final double effortCost = cost.cost(
            mock(Fisher.class),
            mock(FishState.class),
            record,
            0d,
            100
        );

        Assertions.assertEquals(effortCost, 60, .001);

    }

    @Test
    public void additionalCostCorrect() {

        EffortCost cost = new EffortCost(10);
        Assertions.assertEquals(20, cost.expectedAdditionalCosts(
            mock(Fisher.class),
            999,
            2,
            -999
        ), .001);


    }
}
