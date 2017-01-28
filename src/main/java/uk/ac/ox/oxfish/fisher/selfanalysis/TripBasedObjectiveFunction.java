package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.List;

/**
 * A simple abstract class that judges the objective given the last or secondlast trip. How to turn the tripRecord
 * into a utility function depends on the subclasses
 * Created by carrknight on 3/24/16.
 */
public abstract class TripBasedObjectiveFunction implements ObjectiveFunction<Fisher> {


    /**
     * compute current fitness of the agent
     *
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observed) {
        TripRecord lastFinishedTrip = observed.getLastFinishedTrip();
        return lastFinishedTrip == null ? Double.NaN :
                FishStateUtilities.round(
                        extractUtilityFromTrip(lastFinishedTrip,observed )
                );
    }

    /**
     * compute the fitness of the agent "in the previous step"; How far back that is
     * depends on the objective function itself
     *
     * @param observed the agent whose fitness we want
     * @return a fitness value: the higher the better
     */
    @Override
    public double computePreviousFitness(Fisher observed) {
        //get the second last completed trip
        List<TripRecord> finishedTrips = observed.getFinishedTrips();
        if(finishedTrips.size() >= 2)
            return FishStateUtilities.round(
                    extractUtilityFromTrip(finishedTrips.get(finishedTrips.size() - 2),observed )
            );
        else
            return Double.NaN;

    }


    abstract protected double extractUtilityFromTrip(TripRecord tripRecord, Fisher fisher);
}
