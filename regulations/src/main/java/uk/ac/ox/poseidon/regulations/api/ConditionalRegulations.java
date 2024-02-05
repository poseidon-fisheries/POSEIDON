package uk.ac.ox.poseidon.regulations.api;

public interface ConditionalRegulations extends Regulations {
    Condition getCondition();

    Regulations getRegulationIfTrue();

    Regulations getRegulationIfFalse();
}
