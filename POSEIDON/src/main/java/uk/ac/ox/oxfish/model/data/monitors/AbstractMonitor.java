/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.data.monitors;

import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;
import uk.ac.ox.oxfish.utility.FishStateSteppable;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.model.StepOrder.DATA_RESET;

abstract public class AbstractMonitor<O, V, Q extends Quantity<Q>> implements Monitor<O, V, Q>, Gatherer<FishState> {

    private static final long serialVersionUID = 7844804465386740854L;

    private final IntervalPolicy resetInterval;
    private final String baseName;
    private final Supplier<Accumulator<V>> accumulatorSupplier;
    private final Unit<Q> unit;
    private final String yLabel;
    private final FishStateSteppable resetter;
    private Stoppable stoppable = null;
    private Accumulator<V> accumulator;

    AbstractMonitor(
        final String baseName,
        final IntervalPolicy resetInterval,
        final Supplier<Accumulator<V>> accumulatorSupplier,
        final Unit<Q> unit,
        final String yLabel
    ) {
        this.resetInterval = checkNotNull(resetInterval);
        this.baseName = baseName;
        this.accumulatorSupplier = checkNotNull(accumulatorSupplier);
        this.unit = checkNotNull(unit);
        this.yLabel = yLabel;
        this.accumulator = this.accumulatorSupplier.get();
        this.resetter = __ -> this.accumulator = this.accumulatorSupplier.get();
    }

    public String getYLabel() {
        return yLabel;
    }

    public String getBaseName() {
        return baseName;
    }

    @Override
    public Unit<Q> getUnit() {
        return unit;
    }

    public Accumulator<V> getAccumulator() {
        return accumulator;
    }

    @Override
    public Double apply(final FishState fishState) {
        return accumulator.applyAsDouble(fishState);
    }

    @Override
    public void registerWith(final TimeSeries<FishState> timeSeries) {
        if (baseName != null) // a null baseName indicates we don't want to register the accumulator
            timeSeries.registerGatherer(
                accumulator.makeName(baseName),
                this,
                0.0,
                unit,
                yLabel
            );
    }

    @Override
    public void start(final FishState fishState) {
        checkState(stoppable == null, "Already started!");
        stoppable = fishState.schedulePerPolicy(resetter, DATA_RESET, resetInterval);
    }

    @Override
    public void observe(final O observable) {
        checkState(stoppable != null, "Not started!");
        extractValues(observable).forEach(accumulator::accumulate);
    }

}
