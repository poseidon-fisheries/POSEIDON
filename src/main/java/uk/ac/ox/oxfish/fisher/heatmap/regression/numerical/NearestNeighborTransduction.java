package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
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
public class NearestNeighborTransduction implements NumericalGeographicalRegression {



    private final HashMap<SeaTile,GeographicalObservation<Double>> closestNeighborForNow;

    private final static GeographicalObservation<Double> PLACEHOLDER = new GeographicalObservation<Double>(null,-1d,Double.NaN);

    private final RegressionDistance distance;


    public NearestNeighborTransduction(
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
    public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {

        return closestNeighborForNow.getOrDefault(tile,PLACEHOLDER).getValue();
    }


    @Override
    public void addObservation(GeographicalObservation<Double> newObservation, Fisher fisher) {

        //go through all the tiles
        for(SeaTile tile : closestNeighborForNow.keySet())
        {
            //if the new observation is closer than the old one this is your new closest observation
            GeographicalObservation oldObservation = closestNeighborForNow.get(tile);
            if(oldObservation == PLACEHOLDER || (
                    distance.distance(fisher, tile, newObservation.getTime(), newObservation) <
                            distance.distance(fisher, tile, newObservation.getTime(), oldObservation)))
                closestNeighborForNow.put(tile,newObservation);
        }
    }

    //ignored

    @Override
    public void start(FishState model) {

    }

    //ignored

    @Override
    public void turnOff() {

    }
}
