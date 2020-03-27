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
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;

abstract public class AbstractMonitor<O, V> implements Monitor<O, V> {

    private final IntervalPolicy resetInterval;
    private final String baseName;
    private final Supplier<Accumulator<V>> accumulatorSupplier;
    private Accumulator<V> accumulator;
    private Stoppable stoppable = null;

    AbstractMonitor(
        IntervalPolicy resetInterval,
        String baseName,
        Supplier<Accumulator<V>> accumulatorSupplier
    ) {
        this.resetInterval = resetInterval;
        this.baseName = baseName;
        this.accumulatorSupplier = accumulatorSupplier;
        this.accumulator = accumulatorSupplier.get();
    }

    @Override public double getCurrentValue() { return accumulator.get(); }

    @Override public void start(FishState state) {
        checkState(stoppable == null, this + "is already started!");
        stoppable = state.schedulePerPolicy(this, StepOrder.DATA_RESET, resetInterval);
    }

    @Override public double asGatherer(FishState fishState) { return accumulator.get(); }

    @Override public void registerWith(TimeSeries<FishState> timeSeries) {
        timeSeries.registerGatherer(
            accumulator.makeName(baseName),
            this::asGatherer,
            0.0
        );
    }

    @Override public void step(SimState simState) {
        this.accumulator = this.accumulatorSupplier.get();
    }

    @Override public void observe(O observable) {
        extractValues(observable).forEach(accumulator::accumulate);
    }

}
