package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by carrknight on 7/17/17.
 */
public class PeriodHabitContinuousExtractor implements ObservationExtractor {
    private final MapDiscretization discretization;
    private final int period;



    public PeriodHabitContinuousExtractor(MapDiscretization discretization,
                                       final int period) {
        this.discretization = discretization;
        this.period = period;
    }

    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        //it it has been less than ```period``` days since you went there, you get the habit bonus!
        return  count(model.getDay(),
                      agent.getDiscretizedLocationMemory().getVisits(
                              (discretization.getGroup(tile))
                      ));
    }


    /**
     * goes through all the visits and checks how many happened between today and today-period (bound not included)
     * @param currentDay simulation day
     * @param visits queue containing all visits (last ones are more recent)
     * @return number of visits that occurred between (currentDay-period,currentDay]
     */
    public int count(int currentDay,
                     LinkedList<Integer> visits)
    {
        int sum = 0;
        Iterator<Integer> iterator = visits.descendingIterator();
        while(iterator.hasNext())
        {
            Integer visit = iterator.next();
            if(visit> currentDay-period)
                sum++;
            else
                return sum;
        }
        return sum;


    }

}
