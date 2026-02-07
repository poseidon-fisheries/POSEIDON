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

package uk.ac.ox.poseidon.geography.ports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PortsFromTableFactory<S extends Scope>
    extends RelativeScopeFactory<S, List<Pair<Port, Coordinate>>> {

    Factory<? super S, Table> table;
    String portCodeColumnName;
    String portNameColumnName;
    String longitudeColumnName;
    String latitudeColumnName;

    @Override
    protected List<Pair<Port, Coordinate>> newInstance(final S scope) {
        return table
            .get(scope)
            .stream()
            .map(row -> Pair.of(
                new Port(
                    row.getString(portCodeColumnName),
                    row.getString(portNameColumnName)
                ),
                new Coordinate(
                    row.getNumber(longitudeColumnName),
                    row.getNumber(latitudeColumnName)
                )
            ))
            .toList();
    }
}
