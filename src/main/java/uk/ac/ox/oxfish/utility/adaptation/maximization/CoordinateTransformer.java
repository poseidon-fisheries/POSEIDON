package uk.ac.ox.oxfish.utility.adaptation.maximization;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * object to convert from  double coordinates to actual variables and viceversa
 * @param <T>
 */
public  interface CoordinateTransformer<T>
{
    /**
     * turn a T into PSO coordinates
     */
    double[] toCoordinates(T variable, Fisher fisher, FishState model);

    /**
     * turn coordinates into the variable we want to maximize
     */
    T fromCoordinates(double[] variable, Fisher fisher, FishState model);
}
