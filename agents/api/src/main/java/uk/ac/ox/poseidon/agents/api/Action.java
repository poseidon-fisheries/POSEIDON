package uk.ac.ox.poseidon.agents.api;

import java.time.LocalDateTime;

public interface Action {
    Agent getAgent();

    LocalDateTime getDateTime();
}
