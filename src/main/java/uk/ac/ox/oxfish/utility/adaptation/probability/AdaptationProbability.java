package uk.ac.ox.oxfish.utility.adaptation.probability;

import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The object managing and evolving the adaptation probabilities
 * Created by carrknight on 8/28/15.
 */
public interface AdaptationProbability extends FisherStartable
{

    /**
     * get probability of exploring
     */
    double getExplorationProbability();


    /**
     * get probability of imitating
     */
    double getImitationProbability();

    /**
     * react to what the result of the exploration was and see if it changes your probabilities.
     * @param previousFitness pre-exploration fitness
     * @param currentFitness post-exploration fitness
     */
    void judgeExploration(double previousFitness, double currentFitness);


}
