package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * A function to judge the "fitness" of ourselves or of others; possibly to drive adaptation
 * Created by carrknight on 8/4/15.
 */
public interface ObjectiveFunction<T> {

    /**
     * compute current fitness of some object
     *
     * @param observer the person who is judging the fitness.
     * @param observed the thing whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    double computeCurrentFitness(Fisher observer, T observed);

}
