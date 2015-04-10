package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Point;

/**
 * The "skeletal" distance; delegate seatile distance to grid distance.
 * Created by carrknight on 4/10/15.
 */
public abstract class BaseDistance implements Distance {


    @Override
    public double distance(SeaTile start, SeaTile end)
    {
        return distance(start.getGridX(),start.getGridY(),end.getGridX(),end.getGridY());
    }


}
