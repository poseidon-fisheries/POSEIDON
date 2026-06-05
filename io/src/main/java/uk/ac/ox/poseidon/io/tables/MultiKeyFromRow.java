/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.io.tables;

import org.apache.commons.collections4.keyvalue.MultiKey;
import tech.tablesaw.api.Row;

import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MultiKeyFromRow implements Function<Row, MultiKey<Object>> {

    private final String[] keyColumnNames;

    public MultiKeyFromRow(final List<String> keyColumnNames) {
        checkNotNull(keyColumnNames);
        checkArgument(!keyColumnNames.isEmpty());
        this.keyColumnNames = keyColumnNames.toArray(new String[0]);
    }

    @Override
    public MultiKey<Object> apply(final Row row) {
        final Object[] keyValues = new Object[keyColumnNames.length];
        for (int k = 0; k < keyColumnNames.length; k++) {
            keyValues[k] = row.getObject(keyColumnNames[k]);
        }
        return new MultiKey<>(keyValues);
    }
}
