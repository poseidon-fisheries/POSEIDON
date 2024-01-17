package uk.ac.ox.oxfish.model.data.monitors;

import java.util.Collection;

public interface Monitors<O> {
    Collection<Monitor<O, ?, ?>> getMonitors();
}
