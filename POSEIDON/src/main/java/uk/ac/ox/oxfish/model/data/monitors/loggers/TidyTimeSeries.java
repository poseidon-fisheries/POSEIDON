/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.data.monitors.loggers;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;

public abstract class TidyTimeSeries<T extends TimeSeries<?>> implements RowProvider {

    private final T timeSeries;
    private final Predicate<String> columnNamePredicate;

    TidyTimeSeries(
        final T timeSeries,
        final Predicate<String> columnNamePredicate
    ) {
        this.timeSeries = timeSeries;
        this.columnNamePredicate = columnNamePredicate;
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        return timeSeries
            .getColumns()
            .stream()
            .filter(column -> columnNamePredicate.test(column.getName()))
            .flatMap(column -> range(0, column.size()).mapToObj(index -> makeRow(column, index)))
            .collect(toImmutableList());
    }

    abstract List<Object> makeRow(
        DataColumn column,
        int index
    );

}
