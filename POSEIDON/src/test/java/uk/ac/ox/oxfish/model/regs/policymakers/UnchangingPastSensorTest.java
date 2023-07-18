package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.UnchangingPastSensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UnchangingPastSensorTest {

    @Test
    public void targetIsSetCorrectly() {

        //these number come from the DLMTookkit, SimulatedData, row 1
        final double[] indicators = {0.727146523713908, 1.00488900317951, 1.05670327078653, 1.1620629858966,
            0.701410061340196, 0.914689667756402,
            //only these three matter
            0.85, 1.20, 0.75};


        DataColumn indicatorColumn = new DataColumn("indicator");
        for (double indicator : indicators) {
            indicatorColumn.add(indicator);
        }


        FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getYearlyDataSet().getColumn("indicator")).thenReturn(indicatorColumn);

        UnchangingPastSensor target =
            new UnchangingPastSensor("indicator",
                1.5, 3
            );
        assertEquals(target.scan(state),
            1.4, .0001d
        );


        //make sure it doesn't update
        indicatorColumn.add(1d);
        indicatorColumn.add(2d);
        assertEquals(target.scan(state),
            1.4, .0001d
        );
    }
}