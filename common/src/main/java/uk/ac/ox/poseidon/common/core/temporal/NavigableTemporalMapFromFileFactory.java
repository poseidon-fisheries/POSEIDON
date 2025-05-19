/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.common.core.temporal;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.csv.GroupedRecordProcessorFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class NavigableTemporalMapFromFileFactory<K, V>
    implements ComponentFactory<NavigableTemporalMap<K, V>> {

    private final Function<? super TemporalAccessor, ? extends K> temporalToKey;

    private InputPath filePath;
    private GroupedRecordProcessorFactory<K, V> groupedRecordProcessorFactory;

    public NavigableTemporalMapFromFileFactory(
        final Function<? super TemporalAccessor, ? extends K> temporalToKey,
        final InputPath filePath,
        final GroupedRecordProcessorFactory<K, V> groupedRecordProcessorFactory
    ) {
        this(temporalToKey);
        this.filePath = filePath;
        this.groupedRecordProcessorFactory = groupedRecordProcessorFactory;
    }

    public NavigableTemporalMapFromFileFactory(
        final Function<? super TemporalAccessor, ? extends K> temporalToKey
    ) {
        this.temporalToKey = temporalToKey;
    }

    public GroupedRecordProcessorFactory<K, V> getGroupedRecordProcessorFactory() {
        return groupedRecordProcessorFactory;
    }

    public void setGroupedRecordProcessorFactory(final GroupedRecordProcessorFactory<K, V> groupedRecordProcessorFactory) {
        this.groupedRecordProcessorFactory = groupedRecordProcessorFactory;
    }

    public InputPath getFilePath() {
        return filePath;
    }

    public void setFilePath(final InputPath filePath) {
        this.filePath = filePath;
    }

    @Override
    public NavigableTemporalMap<K, V> apply(final ModelState modelState) {
        return new NavigableTemporalMap<>(
            groupedRecordProcessorFactory
                .apply(modelState)
                .apply(recordStream(filePath.get())),
            temporalToKey
        );
    }
}
