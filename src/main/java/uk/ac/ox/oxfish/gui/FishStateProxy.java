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
