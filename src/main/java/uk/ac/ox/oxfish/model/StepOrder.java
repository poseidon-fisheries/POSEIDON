package uk.ac.ox.oxfish.model;


public enum StepOrder {


    DAWN,

    /**
     * fisher act
     */
    FISHER_PHASE,

    /**
     * biome regenerates
     */
    BIOLOGY_PHASE,


    POLICY_UPDATE,

    /**
     * data is stored in DataSet objects
     */
    INDIVIDUAL_DATA_GATHERING,

    /**
     * aggregate data usually access individual data that has just been stored, so it has to happen later
     */
    AGGREGATE_DATA_GATHERING,

    /**
     * counters get reset to 0. Ready to be written over
     */
    DATA_RESET,

    /**
     * exogenous forces that act when the model has stepped can be used here (a simple GA algorithm for example)
     */
    AFTER_DATA
}
