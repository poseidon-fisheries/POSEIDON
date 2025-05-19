/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
package uk.ac.ox.poseidon.r.adaptors.datasets.timeseries;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Table;
import uk.ac.ox.poseidon.r.adaptors.datasets.DoubleColumn;

class YearlyTimeSeriesDataSetAdaptor extends TimeSeriesDataSetAdaptor {
    private final int startYear;

    YearlyTimeSeriesDataSetAdaptor(
        final TimeSeries<?> timeSeries,
        final String indexColumnName,
        final int startYear
    ) {
        super(timeSeries, indexColumnName);
        this.startYear = startYear;
    }

    @Override
    Table makeTable(final DataColumn dataColumn) {
        final DoubleColumn valueColumn =
            new DoubleColumn("value", dataColumn);
        return new TableAdaptor(
            new YearlyIndexColumn(
                getIndexColumnName(), valueColumn, startYear
            ),
            valueColumn
        );
    }
}
