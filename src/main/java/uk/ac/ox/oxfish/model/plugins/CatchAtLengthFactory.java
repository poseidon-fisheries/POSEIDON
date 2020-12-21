package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * the additional startable that deals with SPRAgents; slightly modified to guarantee
 * that there will be a species name and a survey tag associated with it
 */
public interface CatchAtLengthFactory extends AlgorithmFactory<SPRAgent> {

    public String getSpeciesName();

    public String getSurveyTag();



}
