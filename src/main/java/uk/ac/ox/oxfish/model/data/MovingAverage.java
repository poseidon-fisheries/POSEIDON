package uk.ac.ox.oxfish.model.data;

/**
 An object to compute the moving average of whatever is put in.
 * <p/> It accepts any number but the computations are all done through double value call
 * * Created by carrknight on 8/14/15.
 */
public class MovingAverage<T extends Number> implements Averager<T>{

    final private MovingSum<T> sum;

    /**
     * the constructor that creates the moving average object
     */
    public MovingAverage(int movingAverageSize) {
        sum = new MovingSum<T>(movingAverageSize);
    }



    public double getSmoothedObservation()
    {
        if(!isReady())
        {
            assert sum.numberOfObservations() == 0;
            return Double.NaN;
        }
        assert sum.numberOfObservations()>0;
        assert sum.numberOfObservations() <= sum.getSize();
        return sum.getSmoothedObservation()/(sum.numberOfObservations());


    }



    public String toString() {
        return String.valueOf(getSmoothedObservation());
    }

    /**
     * Add a new observation to the moving average
     */
    public void addObservation(T observation) {
        sum.addObservation(observation);
    }

    public int getSize() {
        return sum.getSize();
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     */
    public boolean isReady() {
        return sum.isReady();
    }

    public int numberOfObservations() {
        return sum.numberOfObservations();
    }
}