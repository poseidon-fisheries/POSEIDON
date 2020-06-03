package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ISlopeTest {


    @Test
    public void usingNumbersFromDLMToolkitToTestISlopeImplementation() {

        //these number come from the DLMTookkit, SimulatedData, row 1
        //SimulatedData@MPrec<-c(NA,NA) needs to be called in DLMToolkit to actually compute it!
        final double[] indicators = {0.727146523713908, 1.00488900317951, 1.05670327078653, 1.1620629858966,
                0.701410061340196, 0.914689667756402, 0.85874709669169, 1.20816538836671, 0.754876423014956};

        final double[] catches = {837.820743292191,1604.98156371324,1378.27771114478,1737.79150132131,1278.73589621442,
                1336.22010040894,935.4526217358,2369.60379856847,1019.16842190158};


        DataColumn indicatorColumn = new DataColumn("indicator");
        DataColumn catchesColumn = new DataColumn("catches");
        for (double indicator : indicators) {
            indicatorColumn.add(indicator);
        }
        for (double catchestoday : catches) {
            catchesColumn.add(catchestoday);
        }

        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(state.getYearlyDataSet().getColumn("indicator")).thenReturn(indicatorColumn);
        when(state.getYearlyDataSet().getColumn("catches")).thenReturn(catchesColumn);

        ISlope slope = new ISlope("catches",
                "indicator",0.4,
                0.8,
                5
                );
        double islopeTac = slope.scan(state);
        System.out.println(islopeTac);

        Assert.assertEquals(1129.152,
                islopeTac,
                .001);
    }
}