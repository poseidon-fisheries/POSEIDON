package uk.ac.ox.oxfish.model;


public enum StepOrder {



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

    AFTER_DATA;
}
