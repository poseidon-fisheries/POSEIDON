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
import uk.ac.ox.poseidon.r.adaptors.datasets.DoubleColumn;

import java.time.LocalDate;

class DailyTimeSeriesDataSetAdaptor extends TimeSeriesDataSetAdaptor {
    private final LocalDate startDate;

    DailyTimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName,
        final LocalDate startDate
    ) {
        super(timeSeries, indexColumnName);
        this.startDate = startDate;
    }

    @Override
    TableAdaptor makeTable(final DataColumn dataColumn) {
        final DoubleColumn valueColumn =
            new DoubleColumn("value", dataColumn);
        return new TableAdaptor(
            new DailyIndexColumn<>(
                getIndexColumnName(),
                valueColumn,
                startDate
            ),
            valueColumn
        );
    }
}
