package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.regulations.api.ActionCounts;

public class PurseSeinerActionContext
    implements ActionCounts {
    @Override
    public int getCount(final Agent agent, final Class<? extends Action> action) {
        // TODO
        throw new RuntimeException("Not implemented");
    }
}
