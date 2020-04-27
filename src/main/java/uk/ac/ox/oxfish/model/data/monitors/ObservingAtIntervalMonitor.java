/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.FishStateSteppable;

import java.util.function.Function;

import static uk.ac.ox.oxfish.model.StepOrder.DAILY_DATA_GATHERING;
import static uk.ac.ox.oxfish.model.StepOrder.YEARLY_DATA_GATHERING;
import static uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy.EVERY_YEAR;

public class ObservingAtIntervalMonitor<O, V> extends MonitorDecorator<O, V> {

    private final IntervalPolicy observationInterval;
    private final FishStateSteppable observingSteppable;

    public ObservingAtIntervalMonitor(
        IntervalPolicy observationInterval,
        Function<FishState, Iterable<O>> observablesExtractor,
        Monitor<O, V> delegate
    ) {
        super(delegate);
        this.observationInterval = observationInterval;
        this.observingSteppable =
            fishState -> observablesExtractor.apply(fishState).forEach(this::observe);
    }

    @Override public void start(FishState fishState) {
        StepOrder stepOrder = observationInterval == EVERY_YEAR ? YEARLY_DATA_GATHERING : DAILY_DATA_GATHERING;
        fishState.schedulePerPolicy(observingSteppable, stepOrder, observationInterval);
        super.start(fishState);
    }

}
