/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class YearlyResultsRowProvider implements RowProvider {

    private final FishState fishState;

    private final List<String> HEADERS = ImmutableList.of(
        "target_name",
        "year",
        "output_value"
    );

    public YearlyResultsRowProvider(
        final FishState fishState
    ) {
        this.fishState = fishState;
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        final int startYear = fishState.getScenario().getStartDate().getYear();
        return fishState
            .getYearlyDataSet()
            .getColumns()
            .stream()
            .map(DataColumn::getName)
            .flatMap(columnName -> getYearlyValues(startYear, columnName))
            .collect(toImmutableList());
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

}
