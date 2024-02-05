package uk.ac.ox.poseidon.agents.api;

import uk.ac.ox.poseidon.common.api.Observer;

public interface ActionCounter extends Observer<Action> {
    void observe(Action observable);

    long getCount(Agent agent, String actionCode);

    ActionCounter copy();
}
