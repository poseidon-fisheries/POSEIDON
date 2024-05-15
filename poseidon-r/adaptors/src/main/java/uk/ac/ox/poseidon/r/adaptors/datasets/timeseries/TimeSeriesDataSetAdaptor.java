/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.r.adaptors.datasets.timeseries;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public abstract class TimeSeriesDataSetAdaptor implements Dataset {

    private final TimeSeries<?> timeSeries;
    private final String indexColumnName;

    TimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName
    ) {
        this.timeSeries = timeSeries;
        this.indexColumnName = indexColumnName;
    }

    String getIndexColumnName() {
        return indexColumnName;
    }

    @Override
    public List<Table> getTables() {
        return dataColumnStream()
            .map(this::makeTable)
            .collect(toImmutableList());
    }

    private Stream<DataColumn> dataColumnStream() {
        return timeSeries.getColumns().stream();
    }

    abstract Table makeTable(final DataColumn dataColumn);

    @Override
    public Table getTable(final String name) {
        return dataColumnStream()
            .filter(dataColumn -> dataColumn.getName().equals(name))
            .findFirst()
            .map(this::makeTable)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format(
                    "Unknown table: %s. Valid tables: %s.",
                    name,
                    getTableNames()
                )
            ));
    }

    @Override
    public List<String> getTableNames() {
        return dataColumnStream()
            .map(DataColumn::getName)
            .collect(toImmutableList());
    }
}
