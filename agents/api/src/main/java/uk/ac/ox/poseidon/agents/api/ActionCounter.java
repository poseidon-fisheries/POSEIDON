package uk.ac.ox.poseidon.agents.api;

import uk.ac.ox.poseidon.common.api.Observer;

public interface ActionCounter extends Observer<Action> {
    void observe(Action observable);

    int getCount(Agent agent, Class<? extends Action> action);
}
