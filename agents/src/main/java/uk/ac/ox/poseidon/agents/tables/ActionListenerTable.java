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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.events.AbstractListener;

import java.util.function.Supplier;

public abstract class ActionListenerTable<A extends Action>
    extends AbstractListener<A>
    implements Supplier<Table> {
    private final StringColumn vesselId = StringColumn.create("vessel_id");
    private final DateTimeColumn actionStart = DateTimeColumn.create("action_start");
    private final DateTimeColumn actionEnd = DateTimeColumn.create("action_end");

    private final Table table =
        Table.create(
            vesselId,
            actionStart,
            actionEnd
        );

    ActionListenerTable(final Class<A> eventClass) {
        super(eventClass);
    }

    @Override
    public void receive(final A action) {
        actionStart.append(action.getStart());
        actionEnd.append(action.getEnd());
        vesselId.append(action.getVessel().getId());
    }

    @SuppressFBWarnings(
        value = "EI",
        justification = "Mutable table willfully exposed; just be careful with it."
    )
    @Override
    public Table get() {
        return table;
    }
}
