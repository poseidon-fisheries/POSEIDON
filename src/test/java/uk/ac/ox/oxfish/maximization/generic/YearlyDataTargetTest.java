package uk.ac.ox.oxfish.maximization.generic;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YearlyDataTargetTest {

    @Test
    public void zeroError() {
        YearlyDataTarget target = new YearlyDataTarget(
                Paths.get("inputs","tests","landings3.csv").toString(),
                "fakeData",
                true,
                1
        );

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        assertEquals(target.computeError(model),0,.0001);

    }

    @Test
    public void tenError() {
        YearlyDataTarget target = new YearlyDataTarget(
                Paths.get("inputs","tests","landings3.csv").toString(),
                "fakeData",
                true,
                1
        );

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(90d);
        fakeData.add(90d);
        fakeData.add(90d);
        fakeData.add(90d);

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        assertEquals(target.computeError(model),.1,.0001);

        //if you decrease coefficient of variation, you will weigh your error more!
        target.setCoefficientOfVariation(.1);
        assertEquals(target.computeError(model),1,.0001);


    }
}