package uk.ac.ox.poseidon.agents.api;

import java.util.Set;

public interface Agent {
    String getId();

    Set<String> getTags();
}
