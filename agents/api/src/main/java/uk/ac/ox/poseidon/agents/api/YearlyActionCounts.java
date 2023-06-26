package uk.ac.ox.poseidon.agents.api;

public interface YearlyActionCounts {
    int getCount(int year, Agent agent, Class<? extends Action> action);
}
