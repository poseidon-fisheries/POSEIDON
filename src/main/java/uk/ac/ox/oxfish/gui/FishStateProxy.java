package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyDataSet;

/**
 * This is a lofty name for a simple class. Basically takes all the variables
 * that I want to be displayed in the "aggregate" tab of the gui and provides
 * getters and setters so that the GUI reads only these.
 * Created by carrknight on 6/16/15.
 */
public class FishStateProxy {

    private final FishStateDailyDataSet dailyDataSet;

    public FishStateProxy(FishState state) {
        dailyDataSet = state.getDailyDataSet();
    }

    public FishStateDailyDataSet getDailyDataSet() {
        return dailyDataSet;
    }
}
