package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.Agent;

public interface ActionCounts {
    int getCount(Agent agent, Class<? extends Action> action);
}
