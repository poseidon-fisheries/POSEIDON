package uk.ac.ox.oxfish.model;


public enum StepOrder {




    BEFORE_FISHER_PHASE,

    /**
     * fisher act
     */
    FISHER_PHASE,

    AFTER_FISHER_PHASE,

    /**
     * data is stored in DataSet objects
     */
    DATA_GATHERING,

    /**
     * counters get reset to 0. That's how the day starts
     */
    DATA_RESET,

    AFTER_DATA;
}
