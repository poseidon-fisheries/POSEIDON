package uk.ac.ox.poseidon.agents.tables;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.events.AbstractListener;

import java.util.function.Supplier;

public abstract class ListenerTable<E>
    extends AbstractListener<E>
    implements Supplier<Table> {

    protected final Table table = Table.create();

    protected ListenerTable(
        final Class<E> eventClass
    ) {
        super(eventClass);
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
