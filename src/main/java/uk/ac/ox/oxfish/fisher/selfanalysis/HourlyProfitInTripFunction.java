package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.List;

/**
 * 
 * An objective function that judges people by their latest finished trip profits per hour
 * Created by carrknight on 8/7/15.
 */
public class HourlyProfitInTripFunction implements ObjectiveFunction<Fisher> 
{


    /**
     * whether we should count opportunity costs when it comes to decisions
     */
    private final boolean includingOpportunityCosts;

    public HourlyProfitInTripFunction(boolean includingOpportunityCosts) {
        this.includingOpportunityCosts = includingOpportunityCosts;
    }

    public HourlyProfitInTripFunction() {
        this(true);
    }

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
                FishStateUtilities.round(lastFinishedTrip.getProfitPerHour(includingOpportunityCosts));
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
                    finishedTrips.get(finishedTrips.size() - 2).getProfitPerHour(includingOpportunityCosts)
            );
        else
            return Double.NaN;

    }
}
