/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carrknight on 6/20/17.
 */
public class TripLimitsFactory implements AlgorithmFactory<MultiQuotaRegulation> {


    private HashMap<String, Double> limits = new HashMap<>();

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
    public MultiQuotaRegulation apply(final FishState state) {
        //set up the quotas
        final double[] quotas = new double[state.getSpecies().size()];
        //anything not specified is not protected!
        Arrays.fill(quotas, Double.POSITIVE_INFINITY);

        //create array
        for (final Map.Entry<String, Double> limit : limits.entrySet()) {
            quotas[state.getBiology().getSpeciesByCaseInsensitiveName(limit.getKey()).getIndex()]
                =
                limit.getValue();

        }

        //return it!
        return new MultiQuotaRegulation(
            quotas, state,
            (int) tripLimitPeriod.applyAsDouble(state.getRandom())
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
    public void setLimits(final HashMap<String, Double> limits) {
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
    public void setTripLimitPeriod(final DoubleParameter tripLimitPeriod) {
        this.tripLimitPeriod = tripLimitPeriod;
    }
}
