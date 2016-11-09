package uk.ac.ox.oxfish.model.data;

/**
 * Any class that keeps the average
 * Created by carrknight on 11/9/16.
 */
public interface Averager<T> {


    public void addObservation(T observation);


    /**
     * returns the average
     */
    public double getSmoothedObservation();




}
