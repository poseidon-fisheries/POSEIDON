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

import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;

import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;

public class GridsByDateFromFileFactory
    extends GridsByTemporalFromFileFactory<LocalDate> {
    @SuppressWarnings("unused")
    public GridsByDateFromFileFactory() {
    }

    public GridsByDateFromFileFactory(
        final InputPath filePath,
        final MapExtentFactory mapExtentFactory
    ) {
        super(
            filePath,
            mapExtentFactory
        );
    }

    @SuppressWarnings("unused")
    public GridsByDateFromFileFactory(
        final InputPath filePath,
        final StringParameter groupColumnName,
        final StringParameter longitudeColumnName,
        final StringParameter latitudeColumnName,
        final StringParameter valueColumnName,
        final MapExtentFactory mapExtentFactory
    ) {
        super(
            filePath,
            mapExtentFactory, groupColumnName,
            longitudeColumnName,
            latitudeColumnName,
            valueColumnName
        );
    }

    @Override
    LocalDate temporalToKey(final TemporalAccessor temporalAccessor) {
        return LocalDate.from(temporalAccessor);
    }

    @Override
    LocalDate readGroupColumn(final String groupColumnValue) {
        return LocalDate.parse(groupColumnValue);
    }
}

