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
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import java.util.function.Function;

public class ObservingOnGatherMonitor<O, V> extends MonitorDecorator<O, V> {

    private final Function<FishState, Iterable<O>> observablesExtractor;

    public ObservingOnGatherMonitor(
        Function<FishState, Iterable<O>> observablesExtractor,
        Monitor<O, V> delegate
    ) {
        super(delegate);
        this.observablesExtractor = observablesExtractor;
    }

    @Override public void registerWith(TimeSeries<FishState> timeSeries) {
        timeSeries.registerGatherer(
            getAccumulator().makeName(getBaseName()),
            this::asGatherer,
            0.0
        );
        if (getDelegate() instanceof GroupingMonitor)
            // ugh... surely there is a way to do this without a cast...
            ((GroupingMonitor<?, O, V>) getDelegate()).registerSubMonitorsWith(timeSeries);
    }

    @Override public double asGatherer(FishState fishState) {
        observablesExtractor.apply(fishState).forEach(this::observe);
        return super.asGatherer(fishState);
    }

}
