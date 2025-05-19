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

package uk.ac.ox.poseidon.common.core.csv;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;

import java.util.function.Function;

public class GroupedRecordProcessorFactory<G, V>
    implements ComponentFactory<GroupedRecordProcessor<G, V>> {

    private final Function<? super String, ? extends G> groupColumnReader;
    private StringParameter groupColumnName;
    private ComponentFactory<RecordProcessor<V>> downstreamRecordProcessor;

    public GroupedRecordProcessorFactory(
        final Function<? super String, ? extends G> groupColumnReader
    ) {
        this.groupColumnReader = groupColumnReader;
    }

    public GroupedRecordProcessorFactory(
        final Function<String, G> groupColumnReader,
        final StringParameter groupColumnName,
        final ComponentFactory<RecordProcessor<V>> downstreamRecordProcessor
    ) {
        this.groupColumnReader = groupColumnReader;
        this.groupColumnName = groupColumnName;
        this.downstreamRecordProcessor = downstreamRecordProcessor;
    }

    public ComponentFactory<RecordProcessor<V>> getDownstreamRecordProcessor() {
        return downstreamRecordProcessor;
    }

    public void setDownstreamRecordProcessor(final ComponentFactory<RecordProcessor<V>> downstreamRecordProcessor) {
        this.downstreamRecordProcessor = downstreamRecordProcessor;
    }

    public StringParameter getGroupColumnName() {
        return groupColumnName;
    }

    public void setGroupColumnName(final StringParameter groupColumnName) {
        this.groupColumnName = groupColumnName;
    }

    @Override
    public GroupedRecordProcessor<G, V> apply(final ModelState modelState) {
        return new GroupedRecordProcessor<>(
            groupColumnName.getValue(),
            groupColumnReader,
            downstreamRecordProcessor.apply(modelState)
        );
    }

}
