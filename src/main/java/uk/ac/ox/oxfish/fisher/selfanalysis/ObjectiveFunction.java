package uk.ac.ox.oxfish.fisher.selfanalysis;

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
    double computeCurrentFitness(T observed);

}
