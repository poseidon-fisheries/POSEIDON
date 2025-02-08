/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.tables;

import com.vividsolutions.jts.geom.Coordinate;
import tech.tablesaw.api.DoubleColumn;
import uk.ac.ox.poseidon.agents.behaviours.SpatialAction;

public abstract class SpatialActionListenerTable<A extends SpatialAction> extends ActionListenerTable<A> {
    private final DoubleColumn lon = DoubleColumn.create("lon");
    private final DoubleColumn lat = DoubleColumn.create("lat");

    public SpatialActionListenerTable(
        final Class<A> eventClass
    ) {
        super(eventClass);
        get().addColumns(lon, lat);
    }

    @Override
    public void receive(final A action) {
        super.receive(action);
        final Coordinate coordinate = action.getCoordinate();
        lon.append(coordinate.x);
        lat.append(coordinate.y);
    }
}
