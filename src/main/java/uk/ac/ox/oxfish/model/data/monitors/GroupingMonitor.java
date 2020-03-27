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
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.UnaryOperator.identity;

public class GroupingMonitor<G, O, V> extends AbstractMonitor<O, V> {

    private final Map<G, Monitor<O, V>> subMonitors;
    private final Function<? super O, Collection<G>> groupsExtractor;

    private GroupingMonitor(
        IntervalPolicy intervalPolicy,
        String baseName,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Function<? super O, Collection<G>> groupsExtractor,
        Map<G, Monitor<O, V>> subMonitors
    ) {
        super(intervalPolicy, baseName, accumulatorSupplier);
        this.groupsExtractor = groupsExtractor;
        this.subMonitors = subMonitors;
    }

    GroupingMonitor(
        IntervalPolicy intervalPolicy,
        String baseName,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Function<? super O, Collection<G>> groupsExtractor,
        Collection<G> groups,
        Function<? super G, ? extends Monitor<O, V>> groupMonitorBuilder
    ) {
        this(
            intervalPolicy,
            baseName,
            accumulatorSupplier, groupsExtractor,
            groups.stream().collect(toImmutableMap(
                identity(),
                groupMonitorBuilder
            ))
        );
    }

    @Override public Iterable<V> extractValues(O observable) {
        return groupsExtractor
            .apply(observable)
            .stream()
            .map(subMonitors::get)
            .flatMap(monitor -> stream(monitor.extractValues(observable)))
            ::iterator;
    }

    @Override public void observe(O observable) {
        super.observe(observable);
        groupsExtractor
            .apply(observable)
            .forEach(g -> subMonitors.get(g).observe(observable));
    }

    @Override public void registerWith(TimeSeries<FishState> timeSeries) {
        super.registerWith(timeSeries);
        subMonitors.values().forEach(subMonitor -> subMonitor.registerWith(timeSeries));
    }

    public Optional<Monitor<O, V>> getSubMonitor(G group) {
        return Optional.ofNullable(subMonitors.get(group));
    }

}
