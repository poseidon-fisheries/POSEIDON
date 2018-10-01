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
                1,
                1,
                false);

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
                1,
                1,
                false);

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

    @Test
    public void nonSTD() {
        YearlyDataTarget target = new YearlyDataTarget(
                Paths.get("inputs","tests","landings2.csv").toString(),
                "fakeData",
                false,
                -1,
                1,
                false);

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        DataColumn fakeData = new DataColumn("fakeData");
        double[] input = {324214,
                324215,
                324216,
                324217,
                324218,
                324219,
                324220,
                324221,
                324222,
                324223,
                324224,
                324225,
                324226,
                324227,
                0};
        for (double toAdd : input) {
            fakeData.add(toAdd);
        }


        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        assertEquals(target.computeError(model),324228/15d,.0001);

        //if you decrease coefficient of variation, you will weigh your error more!
        target.setCoefficientOfVariation(Double.NaN);
        assertEquals(target.computeError(model),324228/15d,.0001);

        target.setCoefficientOfVariation(0);
        assertEquals(target.computeError(model),324228/15d,.0001);

    }

    @Test
    public void squared() {
        YearlyDataTarget target = new YearlyDataTarget(
                Paths.get("inputs","tests","landings2.csv").toString(),
                "fakeData",
                false,
                -1,
                2,
                false);

        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        DataColumn fakeData = new DataColumn("fakeData");
        double[] input = {324214,
                324215,
                324216,
                324217,
                324218,
                324219,
                324220,
                324221,
                324222,
                324223,
                324224,
                324225,
                324226,
                324227,
                0};
        for (double toAdd : input) {
            fakeData.add(toAdd);
        }


        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        assertEquals(target.computeError(model),105123795984d/15d,.0001);

        //if you decrease coefficient of variation, you will weigh your error more!
        target.setCoefficientOfVariation(Double.NaN);
        assertEquals(target.computeError(model),105123795984d/15d,.0001);

        target.setCoefficientOfVariation(0);
        assertEquals(target.computeError(model),105123795984d/15d,.0001);

    }
}