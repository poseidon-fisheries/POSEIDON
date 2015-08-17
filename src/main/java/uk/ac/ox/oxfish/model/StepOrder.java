package uk.ac.ox.oxfish.model;


public enum StepOrder {


    DAWN(false),

    /**
     * fisher act
     */
    FISHER_PHASE(true),

    /**
     * biome regenerates
     */
    BIOLOGY_PHASE(true),


    POLICY_UPDATE(true),

    /**
     * data is stored in DataSet objects
     */
    INDIVIDUAL_DATA_GATHERING(false),

    /**
     * aggregate data usually access individual data that has just been stored, so it has to happen later
     */
    AGGREGATE_DATA_GATHERING(false),

    /**
     * counters get reset to 0. Ready to be written over
     */
    DATA_RESET(false),

    /**
     * exogenous forces that act when the model has stepped can be used here (a simple GA algorithm for example)
     */
    AFTER_DATA(true);


    private final boolean toRandomize;

    StepOrder(boolean shouldBeRandomized) {
        this.toRandomize = shouldBeRandomized;
    }


    public boolean isToRandomize() {
        return toRandomize;
    }
}
