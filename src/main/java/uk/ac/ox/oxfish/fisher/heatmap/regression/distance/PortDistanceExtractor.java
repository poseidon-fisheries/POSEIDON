package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Differences in distance from port is what defines this distance
 * Created by carrknight on 7/7/16.
 */
public class PortDistanceExtractor implements ObservationExtractor {



    private final Distance geographicalDistance;

    private final NauticalMap map;

    public PortDistanceExtractor(Distance geographicalDistance,
                                 NauticalMap map) {
        this.geographicalDistance = geographicalDistance;
        this.map = map;
    }

    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        SeaTile portLocation = agent.getHomePort().getLocation();

        return geographicalDistance.distance(portLocation,tile,map)+1d;


    }


}
