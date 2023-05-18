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

package uk.ac.ox.oxfish.model.market;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;

import java.util.LinkedList;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 11/29/16.
 */
public class GasPriceDailyScheduleTest {


    @Test
    public void gasPriceGetsScheduled() throws Exception {

        LinkedList<Double> gasSchedule = Lists.newLinkedList();
        gasSchedule.add(1d);
        gasSchedule.add(2d);
        gasSchedule.add(3d);

        Port port1 = mock(Port.class);
        Port port2 = mock(Port.class);
        TimeSeriesActuator gasPrice = TimeSeriesActuator.gasPriceDailySchedule(
            gasSchedule, Lists.newArrayList(port1, port2)
        );

        gasPrice.step(mock(FishState.class));
        verify(port1).setGasPricePerLiter(1);
        verify(port2).setGasPricePerLiter(1);

        gasPrice.step(mock(FishState.class));
        verify(port1).setGasPricePerLiter(2);
        verify(port2).setGasPricePerLiter(2);

        gasPrice.step(mock(FishState.class));
        verify(port1).setGasPricePerLiter(3);
        verify(port2).setGasPricePerLiter(3);

        gasPrice.step(mock(FishState.class));
        verify(port1, times(2)).setGasPricePerLiter(1);
        verify(port2, times(2)).setGasPricePerLiter(1);
    }
}