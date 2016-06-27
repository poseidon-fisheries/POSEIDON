package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

/**
 * Simulates the effect on beliefs as the Markov Chain state moves from time t to t+1.
 * Created by carrknight on 6/27/16.
 */
public interface MarkovTransition {


    public MarkovBelief step(MarkovBelief current);

}
