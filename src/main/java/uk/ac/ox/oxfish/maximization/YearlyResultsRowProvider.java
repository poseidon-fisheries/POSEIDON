/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

class YearlyResultsRowProvider implements RowProvider {

    private final FishState fishState;

    private final List<String> HEADERS = ImmutableList.of(
        "target_name",
        "year",
        "output_value"
    );

    YearlyResultsRowProvider(
        final FishState fishState
    ) {
        this.fishState = fishState;
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<List<Object>> getYearlyValues(
        final int startYear,
        final String columnName
    ) {
        return Streams.zip(
            IntStream.iterate(startYear, i -> i + 1).boxed(),
            fishState.getYearlyDataSet().getColumn(columnName).stream(),
            (year, value) -> ImmutableList.of(columnName, year, value)
        );
    }

    @Override
    public Iterable<? extends Collection<?>> getRows() {
        final int startYear = fishState.getStartDate().getYear();
        return fishState
            .getYearlyDataSet()
            .getColumns()
            .stream()
            .map(DataColumn::getName)
            .flatMap(columnName -> getYearlyValues(startYear, columnName))
            .collect(toImmutableList());
    }

}
