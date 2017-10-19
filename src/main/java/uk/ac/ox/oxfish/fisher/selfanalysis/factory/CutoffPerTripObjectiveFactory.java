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

package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.CutoffPerTripObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.ConditionalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates Cutoff Per Trip Objective. First use of ConditionalDoubleParameters
 * Created by carrknight on 1/28/17.
 */
public class CutoffPerTripObjectiveFactory implements AlgorithmFactory<CutoffPerTripObjective> {

    private final ConditionalDoubleParameter lowThreshold = new ConditionalDoubleParameter(false,new FixedDoubleParameter(0));

    private final ConditionalDoubleParameter highThreshold = new ConditionalDoubleParameter(false,new FixedDoubleParameter(0));

    private boolean opportunityCosts = true;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public CutoffPerTripObjective apply(FishState fishState) {
        return new CutoffPerTripObjective(
                new HourlyProfitInTripObjective(opportunityCosts),
                lowThreshold.apply(fishState.getRandom()),
                highThreshold.apply(fishState.getRandom())
        );
    }

    /**
     * Getter for property 'lowThreshold'.
     *
     * @return Value for property 'lowThreshold'.
     */
    public ConditionalDoubleParameter getLowThreshold() {
        return lowThreshold;
    }

    /**
     * Getter for property 'highThreshold'.
     *
     * @return Value for property 'highThreshold'.
     */
    public ConditionalDoubleParameter getHighThreshold() {
        return highThreshold;
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
     * Setter for property 'opportunityCosts'.
     *
     * @param opportunityCosts Value to set for property 'opportunityCosts'.
     */
    public void setOpportunityCosts(boolean opportunityCosts) {
        this.opportunityCosts = opportunityCosts;
    }
}
