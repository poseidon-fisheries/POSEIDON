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

import tech.tablesaw.api.DoubleColumn;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.geography.Coordinate;

public abstract class SpatialActionListenerTable<A extends Action> extends ActionListenerTable<A> {

    private final DoubleColumn startLon = DoubleColumn.create("start_lon");
    private final DoubleColumn startLat = DoubleColumn.create("start_lat");
    private final DoubleColumn endLon = DoubleColumn.create("end_lon");
    private final DoubleColumn endLat = DoubleColumn.create("end_lat");

    public SpatialActionListenerTable(
        final Class<A> eventClass
    ) {
        super(eventClass);
        get().addColumns(startLon, startLat, endLon, endLat);
    }

    @Override
    public void receive(final A action) {
        super.receive(action);
        final Coordinate startCoordinate = action.getStartCoordinate();
        startLat.append(startCoordinate.lon);
        startLon.append(startCoordinate.lat);
        final Coordinate endCoordinate = action.getEndCoordinate();
        endLat.append(endCoordinate.lon);
        endLon.append(endCoordinate.lat);
    }
}
