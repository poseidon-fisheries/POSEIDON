/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.geography;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.csv.GroupedRecordProcessorFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.common.core.temporal.NavigableTemporalMap;
import uk.ac.ox.poseidon.common.core.temporal.NavigableTemporalMapFromFileFactory;

import java.time.temporal.TemporalAccessor;

public abstract class GridsByTemporalFromFileFactory<T>
    implements ComponentFactory<NavigableTemporalMap<T, DoubleGrid>> {
    private static final String DEFAULT_GROUP_COLUMN_NAME = "date";
    private static final String DEFAULT_LONGITUDE_COLUMN_NAME = "lon";
    private static final String DEFAULT_LATITUDE_COLUMN_NAME = "lat";
    private static final String DEFAULT_VALUE_COLUMN_NAME = "value";
    private InputPath filePath;
    private StringParameter groupColumnName;
    private MapExtentFactory mapExtentFactory;
    private StringParameter longitudeColumnName;
    private StringParameter latitudeColumnName;
    private StringParameter valueColumnName;

    public GridsByTemporalFromFileFactory() {
    }

    public GridsByTemporalFromFileFactory(
        final InputPath filePath,
        final MapExtentFactory mapExtentFactory
    ) {
        this(
            filePath,
            mapExtentFactory,
            new StringParameter(DEFAULT_GROUP_COLUMN_NAME),
            new StringParameter(DEFAULT_LONGITUDE_COLUMN_NAME),
            new StringParameter(DEFAULT_LATITUDE_COLUMN_NAME),
            new StringParameter(DEFAULT_VALUE_COLUMN_NAME)
        );
    }

    public GridsByTemporalFromFileFactory(
        final InputPath filePath,
        final MapExtentFactory mapExtentFactory,
        final StringParameter groupColumnName,
        final StringParameter longitudeColumnName,
        final StringParameter latitudeColumnName,
        final StringParameter valueColumnName
    ) {
        this.filePath = filePath;
        this.groupColumnName = groupColumnName;
        this.mapExtentFactory = mapExtentFactory;
        this.longitudeColumnName = longitudeColumnName;
        this.latitudeColumnName = latitudeColumnName;
        this.valueColumnName = valueColumnName;
    }

    public StringParameter getGroupColumnName() {
        return groupColumnName;
    }

    public void setGroupColumnName(final StringParameter groupColumnName) {
        this.groupColumnName = groupColumnName;
    }

    public MapExtentFactory getMapExtentFactory() {
        return mapExtentFactory;
    }

    public void setMapExtentFactory(final MapExtentFactory mapExtentFactory) {
        this.mapExtentFactory = mapExtentFactory;
    }

    public StringParameter getLongitudeColumnName() {
        return longitudeColumnName;
    }

    public void setLongitudeColumnName(final StringParameter longitudeColumnName) {
        this.longitudeColumnName = longitudeColumnName;
    }

    public StringParameter getLatitudeColumnName() {
        return latitudeColumnName;
    }

    public void setLatitudeColumnName(final StringParameter latitudeColumnName) {
        this.latitudeColumnName = latitudeColumnName;
    }

    public StringParameter getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(final StringParameter valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public InputPath getFilePath() {
        return filePath;
    }

    public void setFilePath(final InputPath filePath) {
        this.filePath = filePath;
    }

    @Override
    public NavigableTemporalMap<T, DoubleGrid> apply(final ModelState modelState) {
        return new NavigableTemporalMapFromFileFactory<>(
            this::temporalToKey,
            filePath,
            new GroupedRecordProcessorFactory<>(
                this::readGroupColumn,
                groupColumnName,
                new DoubleGridRecordProcessorFactory(
                    longitudeColumnName, latitudeColumnName, valueColumnName, mapExtentFactory
                )
            )
        ).apply(modelState);
    }

    abstract T temporalToKey(TemporalAccessor temporalAccessor);

    abstract T readGroupColumn(String groupColumnValue);
}

