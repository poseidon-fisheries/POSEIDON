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
import uk.ac.ox.poseidon.r.adaptors.datasets.DateColumn;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map.Entry;

import static com.google.common.collect.Streams.mapWithIndex;

class DailyFisherTimeSeriesDataSetAdaptor extends FisherTimeSeriesDataSetAdaptor {

    DailyFisherTimeSeriesDataSetAdaptor(final FishState fishState) {
        super(fishState);
    }

    @Override
    TimeSeries<Fisher> getFisherTimeSeries(final Fisher fisher) {
        return fisher.getDailyData();
    }

    @Override
    DateColumn makeIndexColumn(
        final Collection<? extends Entry<Fisher, DataColumn>> entries
    ) {
        final LocalDate startDate = fishState.getScenario().getStartDate();
        return new DateColumn(
            "date",
            entries.stream().flatMap(fisherDataColumnEntry ->
                mapWithIndex(
                    fisherDataColumnEntry.getValue().stream(),
                    (__, index) -> startDate.plusDays(index)
                )
            )
        );
    }

}
