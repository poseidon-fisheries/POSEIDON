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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.r.adaptors.datasets.IntegerColumn;
import uk.ac.ox.poseidon.r.adaptors.datasets.RColumn;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Streams.mapWithIndex;

public class YearlyFisherTimeSeriesDataSetAdaptor extends FisherTimeSeriesDataSetAdaptor {
    YearlyFisherTimeSeriesDataSetAdaptor(final FishState fishState) {
        super(fishState);
    }

    @Override
    TimeSeries<Fisher> getFisherTimeSeries(final Fisher fisher) {
        return fisher.getYearlyData();
    }

    @Override
    RColumn<?> makeIndexColumn(final Collection<? extends Map.Entry<Fisher, DataColumn>> entries) {
        final int startYear = fishState.getScenario().getStartDate().getYear();
        return new IntegerColumn(
            "year",
            entries.stream().flatMap(fisherDataColumnEntry ->
                mapWithIndex(
                    fisherDataColumnEntry.getValue().stream(),
                    (__, index) -> startYear + index
                )
            )
        );
    }
}
