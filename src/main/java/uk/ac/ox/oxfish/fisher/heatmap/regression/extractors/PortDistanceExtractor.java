package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Differences in distance from port is what defines this distance
 * Created by carrknight on 7/7/16.
 */
public class PortDistanceExtractor implements ObservationExtractor {


    /**
     * distance metric to use, if null, use map distance instead
     */
    private final Distance geographicalDistance;

    /**
     * offset get summed up to all geographical distances (usually to avoid 0s if you think you are taking logs or things like that)
     */
    private final double offset;


    public PortDistanceExtractor(Distance geographicalDistance, final double offset) {

        this.geographicalDistance = geographicalDistance;
        this.offset = offset;
    }

    public PortDistanceExtractor() {
        this.geographicalDistance = null;
        this.offset = 0;
    }

    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        SeaTile portLocation = agent.getHomePort().getLocation();

        if(geographicalDistance != null)
            return geographicalDistance.distance(portLocation,tile,model.getMap())+offset;
        else
            return model.getMap().distance(portLocation,tile) + offset;


    }


}
