package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

/**
 * If I have been to this area (discretized) over the past ```period``` days, that's 1 otherwise 0
 * Created by carrknight on 2/13/17.
 */
public class PeriodHabitExtractor implements ObservationExtractor {


    private final MapDiscretization discretization;
    private final int period;


    public PeriodHabitExtractor(MapDiscretization discretization,
                                final int period) {
        this.discretization = discretization;
        this.period = period;
    }

    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        //it it has been less than ```period``` days since you went there, you get the habit bonus!
        return  model.getDay() -
                agent.getDiscretizedLocationMemory()
                        .getLastDayVisited()[discretization.getGroup(tile)] < period ?
                1 : 0;
    }
}
