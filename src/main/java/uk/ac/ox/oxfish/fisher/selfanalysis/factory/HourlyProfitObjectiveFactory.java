package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Creates a hourly profit objective function
 * Created by carrknight on 3/24/16.
 */
public class HourlyProfitObjectiveFactory implements AlgorithmFactory<HourlyProfitInTripObjective>
{


    private boolean opportunityCosts = true;

    public HourlyProfitObjectiveFactory(boolean opportunityCosts) {
        this.opportunityCosts = opportunityCosts;
    }

    public HourlyProfitObjectiveFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public HourlyProfitInTripObjective apply(FishState fishState) {
        return new HourlyProfitInTripObjective(opportunityCosts);
    }


    public boolean isOpportunityCosts() {
        return opportunityCosts;
    }

    public void setOpportunityCosts(boolean opportunityCosts) {
        this.opportunityCosts = opportunityCosts;
    }
}
