package uk.ac.ox.oxfish.model.market;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;

import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
                gasSchedule,Lists.newArrayList(port1,port2)
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
        verify(port1,times(2)).setGasPricePerLiter(1);
        verify(port2,times(2)).setGasPricePerLiter(1);
    }
}