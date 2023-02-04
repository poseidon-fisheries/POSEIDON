package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

public class EffortCost implements Cost {


    private double costPerHourSpentFishing;

    public EffortCost(double costPerHourSpentFishing) {
        this.costPerHourSpentFishing = costPerHourSpentFishing;
        Preconditions.checkArgument(costPerHourSpentFishing>0);
        Preconditions.checkArgument(Double.isFinite(costPerHourSpentFishing));
    }

    @Override
    public double cost(Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours) {
        return costPerHourSpentFishing * record.getEffort();
    }

    public double getCostPerHourSpentFishing() {
        return costPerHourSpentFishing;
    }

    public void setCostPerHourSpentFishing(double costPerHourSpentFishing) {
        this.costPerHourSpentFishing = costPerHourSpentFishing;
    }

    @Override
    public double expectedAdditionalCosts(Fisher fisher, double additionalTripHours, double additionalEffortHours, double additionalKmTravelled) {
        return additionalEffortHours * costPerHourSpentFishing;
    }
}
