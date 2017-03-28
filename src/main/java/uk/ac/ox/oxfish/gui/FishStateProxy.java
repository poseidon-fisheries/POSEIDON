package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;

/**
 * This is a lofty name for a simple class. Basically takes all the variables
 * that I want to be displayed in the "aggregate" tab of the gui and provides
 * getters and setters so that the GUI reads only these.
 * Created by carrknight on 6/16/15.
 */
public class FishStateProxy {

    private final FishStateDailyTimeSeries dailyDataSet;

    private final FishStateYearlyTimeSeries yearlyDataSet;

    public FishStateProxy(FishState state) {
        dailyDataSet = state.getDailyDataSet();
        yearlyDataSet = state.getYearlyDataSet();
    }

    public FishStateDailyTimeSeries getDailyDataSet() {
        return dailyDataSet;
    }

    public FishStateYearlyTimeSeries getYearlyDataSet() {
        return yearlyDataSet;
    }
}
