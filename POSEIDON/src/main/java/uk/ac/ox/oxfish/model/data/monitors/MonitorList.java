package uk.ac.ox.oxfish.model.data.monitors;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class MonitorList<O> implements Monitors<O> {

    private final Collection<Monitor<O, ?, ?>> monitors;

    MonitorList(final Stream<? extends Monitor<O, ?, ?>> monitors) {
        this(monitors.collect(toImmutableList()));
    }

    private MonitorList(final Iterable<Monitor<O, ?, ?>> monitors) {
        this.monitors = ImmutableList.copyOf(monitors);
    }

    @Override
    public Collection<Monitor<O, ?, ?>> getMonitors() {
        return monitors;
    }

}
