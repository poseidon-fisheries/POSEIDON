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
import uk.ac.ox.poseidon.agents.behaviours.GridAction;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

public abstract class GridActionListenerTable<A extends GridAction> extends ActionListenerTable<A> {
    private final GridExtent gridExtent;
    private final DoubleColumn lon = DoubleColumn.create("lon");
    private final DoubleColumn lat = DoubleColumn.create("lat");

    public GridActionListenerTable(
        final Class<A> eventClass,
        final GridExtent gridExtent
    ) {
        super(eventClass);
        get().addColumns(lon, lat);
        this.gridExtent = gridExtent;
    }

    @Override
    public void receive(final A action) {
        super.receive(action);
        final Coordinate coordinate = gridExtent.toCoordinate(action.getCell());
        lon.append(coordinate.x);
        lat.append(coordinate.y);
    }
}
