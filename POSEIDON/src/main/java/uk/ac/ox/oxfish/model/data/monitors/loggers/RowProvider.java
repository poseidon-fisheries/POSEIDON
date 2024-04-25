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

import com.univocity.parsers.csv.CsvWriter;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.elementsEqual;
import static com.google.common.collect.Lists.asList;
import static com.google.common.collect.Streams.stream;

public interface RowProvider {

    static void writeRows(
        final CsvWriter csvWriter,
        final Collection<? extends RowProvider> rowProviders
    ) {
        writeRows(csvWriter, rowProviders, 1, true);
    }

    static void writeRows(
        final CsvWriter csvWriter,
        final Collection<? extends RowProvider> rowProviders,
        final int runNumber,
        final boolean writeHeaders
    ) {
        checkArgument(!rowProviders.isEmpty());
        rowProviders.stream().findFirst().ifPresent(first -> {
            final List<String> headers = first.getHeaders();
            checkArgument(rowProviders.stream().allMatch(provider ->
                elementsEqual(provider.getHeaders(), headers))
            );
            if (writeHeaders) {
                csvWriter.writeHeaders(asList("run", headers.toArray()));
            }
            csvWriter.writeRowsAndClose(
                rowProviders.stream()
                    .flatMap(rowsProvider -> stream(rowsProvider.getRows()))
                    .map(values -> asList(runNumber, values.toArray()))
                    ::iterator
            );
        });
    }

    List<String> getHeaders();

    Iterable<? extends List<?>> getRows();

    default boolean isEveryStep() {
        return false;
    }

}
