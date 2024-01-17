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

package uk.ac.ox.oxfish.model.data.monitors.accumulators;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Averager;

import java.util.function.DoubleSupplier;

public abstract class AbstractAveragingAccumulator<V extends Number> implements Accumulator<V>, DoubleSupplier {

    private static final long serialVersionUID = 8405148290266756019L;
    private final boolean ignoreNulls;

    AbstractAveragingAccumulator() {
        this(true);
    }

    AbstractAveragingAccumulator(final boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
    }

    @Override
    public void accumulate(final V value) {
        if (value != null || !ignoreNulls) {
            getAverager().addObservation(value);
        }
    }

    abstract Averager<V> getAverager();

    @Override
    public double applyAsDouble(final FishState fishState) {
        return getAsDouble();
    }

    @Override
    public double getAsDouble() {
        return getAverager().getSmoothedObservation();
    }

}
