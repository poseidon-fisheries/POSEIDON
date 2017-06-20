package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carrknight on 6/20/17.
 */
public class TripLimitsFactory implements AlgorithmFactory<MultiQuotaRegulation> {


    private HashMap<String,Double> limits = new HashMap<>();

    private DoubleParameter tripLimitPeriod = new FixedDoubleParameter(60);

    private String convertedInitialQuotas;

    public TripLimitsFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState state) {
        //set up the quotas
        double[] quotas = new double[state.getSpecies().size()];
        //anything not specified is not protected!
        Arrays.fill(quotas,Double.POSITIVE_INFINITY);

        //create array
        for (Map.Entry<String, Double> limit : limits.entrySet())
        {
            quotas[state.getBiology().getSpecie(limit.getKey()).getIndex()]
                    =
                    limit.getValue();

        }

        //return it!
        return new MultiQuotaRegulation(
                quotas
                ,state,
                 tripLimitPeriod.apply(state.getRandom()).intValue()
        );
    }

    /**
     * Getter for property 'limits'.
     *
     * @return Value for property 'limits'.
     */
    public HashMap<String, Double> getLimits() {
        return limits;
    }

    /**
     * Setter for property 'limits'.
     *
     * @param limits Value to set for property 'limits'.
     */
    public void setLimits(HashMap<String, Double> limits) {
        this.limits = limits;
    }

    /**
     * Getter for property 'tripLimitPeriod'.
     *
     * @return Value for property 'tripLimitPeriod'.
     */
    public DoubleParameter getTripLimitPeriod() {
        return tripLimitPeriod;
    }

    /**
     * Setter for property 'tripLimitPeriod'.
     *
     * @param tripLimitPeriod Value to set for property 'tripLimitPeriod'.
     */
    public void setTripLimitPeriod(DoubleParameter tripLimitPeriod) {
        this.tripLimitPeriod = tripLimitPeriod;
    }
}
