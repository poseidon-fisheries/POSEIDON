package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

/**
 * 
 * An objective function that judges people by their latest finished trip profits per hour
 * Created by carrknight on 8/7/15.
 */
public class HourlyProfitInTripObjective extends TripBasedObjectiveFunction
{


    /**
     * whether we should count opportunity costs when it comes to decisions
     */
    private final boolean includingOpportunityCosts;

    public HourlyProfitInTripObjective(boolean includingOpportunityCosts) {
        this.includingOpportunityCosts = includingOpportunityCosts;
    }

    public HourlyProfitInTripObjective() {
        this(true);
    }


    @Override
    protected double extractUtilityFromTrip(
            Fisher observer, TripRecord tripRecord, Fisher Observed) {
        return tripRecord.getProfitPerHour(includingOpportunityCosts);
    }
}
