package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounts;

public class PurseSeinerActionContext
    implements YearlyActionCounts {

    private final YearlyActionCounter yearlyActionCounter;

    public PurseSeinerActionContext(final YearlyActionCounter yearlyActionCounter) {
        this.yearlyActionCounter = yearlyActionCounter;
    }

    @Override
    public int getCount(final int year, final Agent agent, final Class<? extends Action> action) {
        return yearlyActionCounter.getCount(year, agent, action);
    }
}
