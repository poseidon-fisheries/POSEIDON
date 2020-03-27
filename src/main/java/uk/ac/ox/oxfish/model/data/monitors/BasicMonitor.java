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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

import java.util.function.Function;
import java.util.function.Supplier;

public class BasicMonitor<O, V> extends AbstractMonitor<O, V> {

    private final Function<? super O, V> valueExtractor;

    BasicMonitor(
        IntervalPolicy resetInterval,
        String baseName,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Function<? super O, V> valueExtractor
    ) {
        super(resetInterval, baseName, accumulatorSupplier);
        this.valueExtractor = valueExtractor;
    }

    @Override public Iterable<V> extractValues(O observable) {
        return ImmutableList.of(valueExtractor.apply(observable));
    }

}
