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
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision.Region;

import java.util.function.Function;
import java.util.function.Supplier;

public class PerRegionMonitor<O extends Locatable, V> extends GroupingMonitor<Region, O, V> {

    public PerRegionMonitor(
        IntervalPolicy resetInterval,
        String baseName,
        RegionalDivision regionalDivision,
        Function<? super Region, Function<? super O, V>> valueExtractorBuilder,
        Supplier<Accumulator<V>> accumulatorSupplier
    ) {
        this(
            resetInterval,
            baseName,
            regionalDivision,
            accumulatorSupplier,
            region -> new BasicMonitor<>(
                resetInterval,
                String.format("%s (%s)", baseName, region.getName()),
                accumulatorSupplier, valueExtractorBuilder.apply(region)
            )
        );
    }

    private PerRegionMonitor(
        IntervalPolicy intervalPolicy,
        String baseName,
        RegionalDivision regionalDivision,
        Supplier<Accumulator<V>> accumulatorSupplier,
        Function<? super Region, ? extends Monitor<O, V>> groupMonitorBuilder
    ) {
        super(
            intervalPolicy,
            baseName,
            accumulatorSupplier,
            event -> ImmutableList.of(regionalDivision.getRegion(event.getLocation())),
            regionalDivision.getRegions(),
            groupMonitorBuilder
        );
    }

}
