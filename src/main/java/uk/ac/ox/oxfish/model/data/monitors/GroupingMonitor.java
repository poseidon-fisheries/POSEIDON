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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision.Region;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.UnaryOperator.identity;

public class GroupingMonitor<G, O, V, Q extends Quantity<Q>> extends AbstractMonitor<O, V, Q> {

    private final Map<G, Monitor<O, V, Q>> subMonitors;
    private final Function<? super O, Collection<G>> groupsExtractor;

    @SuppressWarnings("WeakerAccess") public GroupingMonitor(
        String baseName,
        IntervalPolicy intervalPolicy,
        Supplier<Accumulator<V>> masterAccumulatorSupplier,
        Unit<Q> unit,
        final String yLabel,
        Function<? super O, Collection<G>> groupsExtractor,
        Collection<G> groups,
        Function<? super G, ? extends Monitor<O, V, Q>> subMonitorBuilder
    ) {
        this(
            baseName,
            intervalPolicy,
            masterAccumulatorSupplier,
            unit,
            yLabel,
            groupsExtractor,
            groups.stream().collect(toImmutableMap(
                identity(),
                subMonitorBuilder
            ))
        );
    }

    private GroupingMonitor(
        String baseName,
        IntervalPolicy intervalPolicy,
        Supplier<Accumulator<V>> masterAccumulatorSupplier,
        Unit<Q> unit,
        final String yLabel,
        Function<? super O, Collection<G>> groupsExtractor,
        Map<G, Monitor<O, V, Q>> subMonitors
    ) {
        super(baseName, intervalPolicy, masterAccumulatorSupplier, unit, yLabel);
        this.groupsExtractor = groupsExtractor;
        this.subMonitors = subMonitors;
    }

    public static <O, V, Q extends Quantity<Q>> GroupingMonitor<Species, O, V, Q> basicPerSpeciesMonitor(
        String baseName,
        IntervalPolicy resetInterval,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Unit<Q> unit,
        final String yLabel,
        Collection<Species> allSpecies,
        Function<? super Species, Function<? super O, V>> valueExtractorBuilder
    ) {
        return basicGroupingMonitor(
            baseName,
            resetInterval,
            accumulatorSupplier,
            unit,
            yLabel,
            allSpecies,
            species -> String.format("%s %s", species, baseName),
            __ -> allSpecies,
            valueExtractorBuilder
        );
    }

    public static <G, O, V, Q extends Quantity<Q>> GroupingMonitor<G, O, V, Q> basicGroupingMonitor(
        String baseName,
        IntervalPolicy intervalPolicy,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Unit<Q> unit,
        final String yLabel,
        Collection<G> groups,
        Function<? super G, String> groupNameMaker,
        Function<? super O, Collection<G>> groupsExtractor,
        Function<? super G, Function<? super O, V>> valueExtractorBuilder
    ) {
        return new GroupingMonitor<>(
            baseName,
            intervalPolicy,
            accumulatorSupplier,
            unit,
            yLabel,
            groupsExtractor,
            groups.stream().collect(toImmutableMap(
                identity(),
                group -> new BasicMonitor<>(
                    groupNameMaker.apply(group),
                    intervalPolicy,
                    accumulatorSupplier,
                    unit,
                    yLabel,
                    valueExtractorBuilder.apply(group)
                )
            ))
        );
    }

    public static <O extends Locatable, V, Q extends Quantity<Q>> GroupingMonitor<Species, O, V, Q> perSpeciesPerRegionMonitor(
        String baseName,
        IntervalPolicy resetInterval,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Unit<Q> unit,
        final String yLabel,
        Collection<Species> allSpecies,
        Function<? super Species, Function<Region, Function<? super O, V>>> valueExtractorBuilder,
        RegionalDivision regionalDivision
    ) {
        return perSpeciesMonitor(
            baseName,
            resetInterval,
            accumulatorSupplier,
            unit,
            yLabel,
            allSpecies,
            species -> basicPerRegionMonitor(
                String.format("%s %s", species, baseName),
                resetInterval,
                regionalDivision,
                valueExtractorBuilder.apply(species),
                accumulatorSupplier,
                unit,
                yLabel
            )
        );
    }

    private static <O, V, Q extends Quantity<Q>> GroupingMonitor<Species, O, V, Q> perSpeciesMonitor(
        String baseName,
        IntervalPolicy resetInterval,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Unit<Q> unit,
        final String yLabel,
        Collection<Species> allSpecies,
        Function<? super Species, ? extends Monitor<O, V, Q>> subMonitorBuilder
    ) {
        return new GroupingMonitor<>(
            baseName,
            resetInterval,
            accumulatorSupplier,
            unit,
            yLabel,
            __ -> allSpecies,
            allSpecies,
            subMonitorBuilder
        );
    }

    public static <O extends Locatable, V, Q extends Quantity<Q>> GroupingMonitor<Region, O, V, Q> basicPerRegionMonitor(
        String baseName,
        IntervalPolicy resetInterval,
        RegionalDivision regionalDivision,
        Function<? super Region, Function<? super O, V>> valueExtractorBuilder,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Unit<Q> unit,
        final String yLabel
    ) {
        return perRegionMonitor(
            baseName,
            resetInterval,
            regionalDivision,
            accumulatorSupplier,
            unit,
            yLabel,
            region -> new BasicMonitor<>(
                String.format("%s (%s)", baseName, region.getName()),
                resetInterval,
                accumulatorSupplier,
                unit,
                yLabel,
                valueExtractorBuilder.apply(region)
            )
        );

    }

    public static <O extends Locatable, V, Q extends Quantity<Q>> GroupingMonitor<Region, O, V, Q> perRegionMonitor(
        String baseName,
        IntervalPolicy intervalPolicy,
        RegionalDivision regionalDivision,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Unit<Q> unit,
        final String yLabel, Function<? super Region, ? extends Monitor<O, V, Q>> groupMonitorBuilder
    ) {
        return new GroupingMonitor<>(
            baseName,
            intervalPolicy,
            accumulatorSupplier,
            unit,
            yLabel,
            event -> ImmutableList.of(regionalDivision.getRegion(event.getLocation())),
            regionalDivision.getRegions(),
            groupMonitorBuilder
        );
    }

    Map<G, Monitor<O, V, Q>> getSubMonitors() { return subMonitors; }

    @Override public Iterable<V> extractValues(O observable) {
        return groupsExtractor
            .apply(observable)
            .stream()
            .map(subMonitors::get)
            .flatMap(monitor -> stream(monitor.extractValues(observable)))
            ::iterator;
    }

    @Override public void registerWith(TimeSeries<FishState> timeSeries) {
        super.registerWith(timeSeries);
        subMonitors.values().forEach(subMonitor -> subMonitor.registerWith(timeSeries));
    }

    @Override public void start(FishState fishState) {
        super.start(fishState);
        subMonitors.values().forEach(subMonitor -> subMonitor.start(fishState));
    }

    @Override public void observe(O observable) {
        super.observe(observable);
        groupsExtractor
            .apply(observable)
            .forEach(g -> subMonitors.get(g).observe(observable));
    }

    public Optional<Monitor<O, V, Q>> getSubMonitor(G group) {
        return Optional.ofNullable(subMonitors.get(group));
    }

}
