package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;

/**
 * Rather than building a k-d tree to search all the time why don't we just have a map of seatiles we are studying and keep
 * a map seatile---> nearest neighbor associated with it? It might not work for observations with additional information (like habitat and so on)
 * That's the idea here
 * Created by carrknight on 7/5/16.
 */
public class NearestNeighborTransduction implements GeographicalRegression {



    private final HashMap<SeaTile,GeographicalObservation> closestNeighborForNow;

    private final static GeographicalObservation PLACEHOLDER = new GeographicalObservation(null,-1,Double.NaN);

    private final RegressionDistance distance;


    public NearestNeighborTransduction(double exponentialWeight,
                                       NauticalMap map,
                                       RegressionDistance distance) {

        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        closestNeighborForNow = new HashMap<>(tiles.size());
        for(SeaTile tile : tiles)
            closestNeighborForNow.put(tile,PLACEHOLDER);

        this.distance = distance;

    }

    /**
     * returns stored closest best
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, FishState state) {

        return closestNeighborForNow.getOrDefault(tile,PLACEHOLDER).getValue();
    }


    @Override
    public void addObservation(GeographicalObservation newObservation) {

        //go through all the tiles
        for(SeaTile tile : closestNeighborForNow.keySet())
        {
            //if the new observation is closer than the old one this is your new closest observation
            GeographicalObservation oldObservation = closestNeighborForNow.get(tile);
            if(oldObservation == PLACEHOLDER || (
                    distance.distance(tile,newObservation.getTime() , newObservation) <
                            distance.distance(tile,newObservation.getTime() , oldObservation)))
                closestNeighborForNow.put(tile,newObservation);
        }
    }
}
