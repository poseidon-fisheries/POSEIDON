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

package uk.ac.ox.poseidon.geography.ports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toSet;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortFactory extends GlobalScopeFactory<Port> {

    private Factory<? extends PortGrid> portGrid;
    private String code;
    private String name;
    private Factory<? extends Coordinate> coordinateFactory;

    @Override
    protected Port newInstance(final Simulation simulation) {
        final PortGrid portGrid = this.portGrid.get(simulation);
        checkState(
            !portGrid.getPorts().map(Port::getCode).collect(toSet()).contains(code),
            "Port code %s already exists", code
        );
        final Coordinate coordinate = coordinateFactory.get(simulation);
        portGrid.validateLocation(coordinate);
        final Port port = new Port(code, name);
        portGrid.getField().setObjectLocation(port, portGrid.getModelGrid().toCell(coordinate));
        return port;
    }
}
