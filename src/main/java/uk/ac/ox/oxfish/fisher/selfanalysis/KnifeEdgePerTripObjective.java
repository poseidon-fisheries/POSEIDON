package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

/**
 * The simplest kind of satistificing utility function: trip made more than x? utility is 1, else utility is -1
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgePerTripObjective extends TripBasedObjectiveFunction {

    /**
     * should I count opportunity costs?
     */
    private final boolean opportunityCosts;

    /**
     * threshold above (and equal) to which your utility is +1 else is -1
     */
    private final double minimumProfitPerHour;


    public KnifeEdgePerTripObjective(boolean opportunityCosts, double minimumProfitPerHour) {
        this.opportunityCosts = opportunityCosts;
        this.minimumProfitPerHour = minimumProfitPerHour;
    }

    @Override
    protected double extractUtilityFromTrip(
            Fisher observer, TripRecord tripRecord, Fisher Observed) {
        return tripRecord.getProfitPerHour(opportunityCosts) >= minimumProfitPerHour ?
                +1 : -1;
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public boolean isOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * Getter for property 'minimumProfitPerHour'.
     *
     * @return Value for property 'minimumProfitPerHour'.
     */
    public double getMinimumProfitPerHour() {
        return minimumProfitPerHour;
    }
}
