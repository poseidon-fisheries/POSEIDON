package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;

/**
 * an interface to choose which formula we are using to compute SPR
 */
public interface SPRFormula {


    public double computeSPR(
        SPRAgent sprAgent,
        StructuredAbundance abundance
    );


}
