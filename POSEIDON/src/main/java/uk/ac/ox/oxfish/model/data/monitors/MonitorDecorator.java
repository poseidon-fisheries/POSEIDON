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
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

import javax.measure.Quantity;
import javax.measure.Unit;

public abstract class MonitorDecorator<O, V, Q extends Quantity<Q>> implements Monitor<O, V, Q> {

    private final Monitor<O, V, Q> delegate;

    MonitorDecorator(Monitor<O, V, Q> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getBaseName() {
        return delegate.getBaseName();
    }

    @Override
    public Unit<Q> getUnit() {
        return delegate.getUnit();
    }

    @Override
    public Iterable<V> extractValues(O observable) {
        return delegate.extractValues(observable);
    }

    @Override
    public Accumulator<V> getAccumulator() {
        return delegate.getAccumulator();
    }

    @Override
    public void registerWith(TimeSeries<FishState> timeSeries) {
        delegate.registerWith(timeSeries);
    }

    @Override
    public void start(FishState model) {
        delegate.start(model);
    }

    @Override
    public void observe(O observable) {
        delegate.observe(observable);
    }

}
