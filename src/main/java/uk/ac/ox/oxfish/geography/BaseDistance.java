package uk.ac.ox.oxfish.geography;

/**
 * The "skeletal" distance; delegate seatile distance to grid distance.
 * Created by carrknight on 4/10/15.
 */
public abstract class BaseDistance implements Distance {


    @Override
    public double distance(SeaTile start, SeaTile end, NauticalMap map)
    {
        return distance(start.getGridX(),start.getGridY(),end.getGridX(),end.getGridY());
    }

    protected abstract double distance(int startX, int startY, int endX, int endY);


}
