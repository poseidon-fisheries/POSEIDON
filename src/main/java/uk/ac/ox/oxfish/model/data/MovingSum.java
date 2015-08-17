package uk.ac.ox.oxfish.model.data;

/**
 *  Sum up the last x observations fed in
 * Created by carrknight on 8/14/15.
 */



import java.util.LinkedList;


public class MovingSum<T extends Number>
{

    /**
     * Where we keep all the observations
     */
    LinkedList<T> lastElements = new LinkedList<>();

    /**
     * The size of the queue
     */
    final private int size;

    public MovingSum(int size) {
        this.size = size;
    }

    /**
     * Add a new observation to the moving average
     * @param observation number to add
     */
    public void addObservation(T observation){

        //add the last observation
        lastElements.addLast(observation);
        //if the queue is full, remove the first guy
        if(lastElements.size()>size)
            lastElements.removeFirst();

        assert lastElements.size() <=size;


    }


    /**
     * the sum computed so far
     *
     * @return the smoothed observation
     */
    public double getSmoothedObservation()
    {
        if(!isReady())
            return Float.NaN;

        float total  =0;
        for(T element : lastElements)
            total += element.doubleValue();

        return total;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     */
    public boolean isReady() {
        return !lastElements.isEmpty();
    }



    public int getSize() {
        return size;
    }

    public int numberOfObservations(){
        return lastElements.size();
    }
}
