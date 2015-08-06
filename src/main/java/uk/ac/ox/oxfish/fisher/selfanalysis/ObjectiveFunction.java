package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * A function to judge the "fitness" of ourselves or of others; possibly to drive adaptation
 * Created by carrknight on 8/4/15.
 */
public interface ObjectiveFunction<T> {

    /**
     * compute current fitness of the agent
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    public double computeCurrentFitness(T observed);

    /**
     * compute the fitness of the agent "in the previous step"; How far back that is
     * depends on the objective function itself
     * @param observed the agent whose fitness we want
     * @return a fitness value: the higher the better
     */
    public double computePreviousFitness(T observed);

}
