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

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapFromTableFactory<S extends Scope, K, V>
    extends RelativeScopeFactory<S, ImmutableMap<K, V>> {

    private Factory<? super S, Table> table;

    private Factory<? super S, ? extends Function<? super Row, ? extends K>>
        keyBuilder;

    private Factory<? super S, ? extends Function<? super Row, ? extends V>>
        valueBuilder;

    @Override
    protected ImmutableMap<K, V> newInstance(final S scope) {

        final var keyBuilder = this.keyBuilder.get(scope);
        final var valueBuilder = this.valueBuilder.get(scope);
        final var table = this.table.get(scope);
        final int rowCount = table.rowCount();
        final var builder = ImmutableMap.<K, V>builderWithExpectedSize(rowCount);

        for (int r = 0; r < rowCount; r++) {
            final Row row = table.row(r);
            builder.put(keyBuilder.apply(row), valueBuilder.apply(row));
        }

        return builder.build();
    }

}
