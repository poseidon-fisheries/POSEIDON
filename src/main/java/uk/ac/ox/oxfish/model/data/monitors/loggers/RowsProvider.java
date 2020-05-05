/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;
import com.univocity.parsers.csv.CsvWriter;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Streams.stream;

public interface RowsProvider {

    default void writeRows(CsvWriter csvWriter) {
        writeRows(csvWriter, ImmutableList.of(this));
    }

    static void writeRows(CsvWriter csvWriter, Collection<? extends RowsProvider> rowsProviders) {
        checkArgument(!rowsProviders.isEmpty());
        rowsProviders.stream().findFirst().ifPresent(first -> {
            final String[] headers = first.getHeaders();
            checkArgument(rowsProviders.stream().allMatch(provider ->
                Arrays.equals(provider.getHeaders(), headers))
            );
            csvWriter.writeHeaders(headers);
            csvWriter.writeRowsAndClose(
                rowsProviders.stream()
                    .flatMap(rowsProvider -> stream(rowsProvider.getRows()))
                    ::iterator
            );
        });
    }

    String[] getHeaders();
    Iterable<? extends Collection<?>> getRows();

}
