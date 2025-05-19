/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.tables;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.util.function.Supplier;

public abstract class ActionListenerTable<A extends Action>
    extends ListenerTable<A>
    implements Supplier<Table> {

    private final StringColumn vesselId = StringColumn.create("vessel_id");
    private final DateTimeColumn actionStart = DateTimeColumn.create("action_start");
    private final DateTimeColumn actionEnd = DateTimeColumn.create("action_end");

    ActionListenerTable(final Class<A> eventClass) {
        super(eventClass);
        table.addColumns(vesselId, actionStart, actionEnd);
    }

    @Override
    public void receive(final A action) {
        actionStart.append(action.getStartDateTime());
        actionEnd.append(action.getEndDateTime());
        vesselId.append(action.getVessel().getId());
    }

}
