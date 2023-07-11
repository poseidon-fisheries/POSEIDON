package uk.ac.ox.poseidon.agents.api;

public interface YearlyActionCounts {
    long getCount(int year, Agent agent, String actionCode);
}
