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

package uk.ac.ox.poseidon.agents.tables;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.events.AbstractListener;

import java.util.function.Supplier;

/**
 * An {@link AbstractListener} that accumulates the events it receives into a tablesaw
 * {@link Table}, one row per event (or per some finer unit, e.g. one row per species per event —
 * see subclasses), exposed via {@link #get()}. Subclasses add columns to {@link #table} in their
 * constructor and append to them in {@link #receive}.
 *
 * @param <E> the type of event this table listens for
 */
public abstract class ListenerTable<E>
    extends AbstractListener<E>
    implements Supplier<Table> {

    /** The accumulated table; columns are added by subclasses in their constructor. */
    protected final Table table = Table.create();

    /** @param eventClass the type of event this table listens for */
    protected ListenerTable(
        final Class<E> eventClass
    ) {
        super(eventClass);
    }

    /** @return the live, mutable table this listener has been accumulating rows into */
    @SuppressFBWarnings(
        value = "EI",
        justification = "Mutable table willfully exposed; just be careful with it."
    )
    @Override
    public Table get() {
        return table;
    }
}
