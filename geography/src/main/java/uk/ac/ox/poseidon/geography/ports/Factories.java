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

/**
 * Factories for {@link Port}s and {@link PortGrid}s: standalone ports, ports placed at fixed
 * coordinates (directly or read from a table), and ports placed at random suitable locations.
 */
public class Factories {

    private Factories() {}

    /**
     * @param code the port's unique code
     * @param name the port's display name
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a standalone {@link Port}
     * @see Port
     */
    public static PortFactory port(
        final String code,
        final String name
    ) {
        return new PortFactory(code, name);
    }

    /**
     * @param ports              factory for the (port, coordinate) pairs to place
     * @param bathymetricGrid    factory for the bathymetric grid ports are placed relative to
     * @param distanceCalculator factory for the calculator used to find the closest suitable
     *                           land cell when a port's own coordinate isn't valid
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
     * {@link ImmutablePortGrid}
     * @see PortGridFactory
     */
    public static <S extends Scope> PortGridFactory<S> portGrid(
        final Factory<? super S, ? extends List<Pair<Port, Coordinate>>> ports,
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends DistanceCalculator> distanceCalculator
    ) {
        return new PortGridFactory<>(ports, bathymetricGrid, distanceCalculator);
    }

    /**
     * @param table               factory for the table to read
     * @param portCodeColumnName  the name of the column holding port codes
     * @param portNameColumnName  the name of the column holding port names
     * @param longitudeColumnName the name of the column holding port longitudes
     * @param latitudeColumnName  the name of the column holding port latitudes
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the list of (port,
     * coordinate) pairs read from the resolved table
     * @see PortsFromTableFactory
     */
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

    /**
     * @param bathymetricGrid           factory for the bathymetric grid ports are placed on
     * @param idSupplier                factory for the supplier of each new port's code
     * @param numberOfPorts             how many ports to place
     * @param minimumAdjacentWaterTiles the minimum number of active water neighbours a land cell
     *                                  must have to be a candidate port location
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
     * {@link MutablePortGrid} with randomly-placed ports
     * @see RandomLocationsPortGridFactory
     */
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
