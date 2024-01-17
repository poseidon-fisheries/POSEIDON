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

import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

public class BasicMonitor<O, V, Q extends Quantity<Q>> extends AbstractMonitor<O, V, Q> {

    private static final long serialVersionUID = -7516952997092729146L;
    private final Function<? super O, ? extends V> valueExtractor;

    public BasicMonitor(
        final String baseName,
        final IntervalPolicy resetInterval,
        final Supplier<Accumulator<V>> accumulatorSupplier,
        final Unit<Q> unit,
        final String yLabel,
        final Function<? super O, ? extends V> valueExtractor
    ) {
        super(baseName, resetInterval, accumulatorSupplier, unit, yLabel);
        this.valueExtractor = valueExtractor;
    }

    @Override
    public Iterable<V> extractValues(final O observable) {
        return singletonList(valueExtractor.apply(observable));
    }

}
