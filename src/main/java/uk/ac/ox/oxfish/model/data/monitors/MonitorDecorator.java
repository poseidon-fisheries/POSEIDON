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

import sim.engine.SimState;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

public abstract class MonitorDecorator<O, V> implements Monitor<O, V> {

    private final Monitor<O, V> delegate;

    MonitorDecorator(Monitor<O, V> delegate) { this.delegate = delegate; }

    Monitor<O, V> getDelegate() { return delegate; }

    @Override public Accumulator<V> getAccumulator() { return delegate.getAccumulator(); }

    @Override public String getBaseName() { return delegate.getBaseName(); }

    @Override public double getCurrentValue() { return delegate.getCurrentValue(); }

    @Override public Iterable<V> extractValues(O observable) { return delegate.extractValues(observable); }

    @Override public void registerWith(TimeSeries<FishState> timeSeries) {
        delegate.registerWith(timeSeries);
    }

    @Override public double asGatherer(FishState fishState) { return delegate.asGatherer(fishState); }

    @Override public void step(SimState simState) {
        delegate.step(simState);
    }

    @Override public void start(FishState model) {
        delegate.start(model);
    }

    @Override public void turnOff() {
        delegate.turnOff();
    }

    @Override public void observe(O observable) {
        delegate.observe(observable);
    }

}
