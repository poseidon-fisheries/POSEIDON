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

import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

import java.util.List;
import java.util.function.Supplier;

public class Factories {

    private Factories() {}

    public static PortFactory port(
        final String code,
        final String name
    ) {
        return new PortFactory(code, name);
    }

    public static <S extends Scope> PortGridFactory<S> portGrid(
        final Factory<? super S, ? extends List<Pair<Port, Coordinate>>> ports,
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends DistanceCalculator> distanceCalculator
    ) {
        return new PortGridFactory<>(ports, bathymetricGrid, distanceCalculator);
    }

    public static <S extends Scope> PortsFromTableFactory<S> portsFromTable(
        final Factory<? super S, Table> table,
        final String portCodeColumnName,
        final String portNameColumnName,
        final String longitudeColumnName,
        final String latitudeColumnName
    ) {
        return new PortsFromTableFactory<>(
            table,
            portCodeColumnName,
            portNameColumnName,
            longitudeColumnName,
            latitudeColumnName
        );
    }

    public static RandomLocationsPortGridFactory randomLocationsPortGrid(
        final Factory<? super SimulationScope, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super SimulationScope, ? extends Supplier<String>> idSupplier,
        final int numberOfPorts,
        final int minimumAdjacentWaterTiles
    ) {
        return new RandomLocationsPortGridFactory(
            bathymetricGrid,
            idSupplier,
            numberOfPorts,
            minimumAdjacentWaterTiles
        );
    }
}
