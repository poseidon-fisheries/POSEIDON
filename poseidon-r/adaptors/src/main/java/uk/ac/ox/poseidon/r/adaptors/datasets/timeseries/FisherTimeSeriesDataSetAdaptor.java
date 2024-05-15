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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.Table;
import uk.ac.ox.poseidon.r.adaptors.datasets.ArrayColumn;
import uk.ac.ox.poseidon.r.adaptors.datasets.DoubleColumn;
import uk.ac.ox.poseidon.r.adaptors.datasets.RColumn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.poseidon.common.core.Entry.entry;

public abstract class FisherTimeSeriesDataSetAdaptor implements Dataset {
    final FishState fishState;

    FisherTimeSeriesDataSetAdaptor(final FishState fishState) {
        this.fishState = fishState;
    }

    @Override
    public List<Table> getTables() {
        return getTimeSeries().keySet().stream().map(this::getTable).collect(toImmutableList());
    }

    private Map<String, List<Entry<Fisher, DataColumn>>> getTimeSeries() {
        return fishState
            .getFishers()
            .stream()
            .flatMap(fisher ->
                getFisherTimeSeries(fisher).getColumns().stream().map(dataColumn ->
                    entry(fisher, dataColumn)
                )
            ).collect(groupingBy(fisherDataColumnEntry ->
                fisherDataColumnEntry.getValue().getName()
            ));
    }

    @Override
    public Table getTable(final String name) {
        return Optional
            .ofNullable(getTimeSeries().get(name))
            .map(entries ->
                new TableAdaptor(
                    makeIndexColumn(entries),
                    new ArrayColumn<>(
                        "agent_id",
                        entries.stream()
                            .flatMap(fisherDataColumnEntry ->
                                fisherDataColumnEntry
                                    .getValue()
                                    .stream()
                                    .map(value -> fisherDataColumnEntry.getKey().getId())
                            )
                            .toArray(String[]::new)
                    ),
                    new DoubleColumn(
                        "value",
                        entries.stream().flatMap(fisherDataColumnEntry ->
                            fisherDataColumnEntry.getValue().stream()
                        )
                    )
                )
            )
            .orElseThrow(() -> new IllegalArgumentException(
                String.format(
                    "Unknown table: %s. Valid tables: %s.",
                    name,
                    getTableNames()
                )
            ));
    }

    abstract TimeSeries<Fisher> getFisherTimeSeries(Fisher fisher);

    abstract RColumn<?> makeIndexColumn(
        Collection<? extends Entry<Fisher, DataColumn>> entries
    );

    @Override
    public List<String> getTableNames() {
        return ImmutableList.copyOf(getTimeSeries().keySet());
    }
}
