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
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.KnifeEdgeCashflowFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgeCashflowObjectiveTest {


    //copy of the cashflow test
    @Test
    public void correctlyKnifesCashFlow() throws Exception {

        //let's do it every 6 days
        CashFlowObjective objective = new CashFlowObjective(6);
        Fisher fisher = mock(Fisher.class);
        FisherDailyTimeSeries data = new FisherDailyTimeSeries();
        data.start(mock(FishState.class), fisher);
        when(fisher.getDailyData()).thenReturn(data);
        when(fisher.balanceXDaysAgo(anyInt())).thenCallRealMethod();

        KnifeEdgeCashflowFactory factory = new KnifeEdgeCashflowFactory();
        factory.setThreshold(new FixedDoubleParameter(6));
        factory.setPeriod(new FixedDoubleParameter(6));
        ;
        KnifeEdgeCashflowObjective knifeEdge = factory.apply(mock(FishState.class));
        factory.setThreshold(new FixedDoubleParameter(7));
        KnifeEdgeCashflowObjective knifeEdge2 = factory.apply(mock(FishState.class));

        for (int i = 0; i < 100; i++) {
            when(fisher.getBankBalance()).thenReturn(((double) i));
            data.step(mock(FishState.class));

        }
        //cash should now be 6, 6 days ago it was 6
        Assertions.assertEquals(6, objective.computeCurrentFitness(fisher, fisher), .0001);
        Assertions.assertEquals(1, knifeEdge.computeCurrentFitness(fisher, fisher), .0001);
        Assertions.assertEquals(-1, knifeEdge2.computeCurrentFitness(fisher, fisher), .0001);

        //let's add some garbage
        for (int i = 0; i < 6; i++) {
            when(fisher.getBankBalance()).thenReturn(0d);
            data.step(mock(FishState.class));
        }
        //now it should be -99
        Assertions.assertEquals(-99, objective.computeCurrentFitness(fisher, fisher), .0001);
        Assertions.assertEquals(-1, knifeEdge.computeCurrentFitness(fisher, fisher), .0001);
        Assertions.assertEquals(-1, knifeEdge2.computeCurrentFitness(fisher, fisher), .0001);

    }

}
