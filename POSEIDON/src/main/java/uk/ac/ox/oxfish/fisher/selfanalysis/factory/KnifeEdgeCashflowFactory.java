/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.KnifeEdgeCashflowObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgeCashflowFactory implements AlgorithmFactory<KnifeEdgeCashflowObjective> {


    private DoubleParameter period = new FixedDoubleParameter(365);

    private DoubleParameter threshold = new FixedDoubleParameter(10d);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public KnifeEdgeCashflowObjective apply(final FishState fishState) {
        final CashFlowObjectiveFactory factory = new CashFlowObjectiveFactory(period);
        return new KnifeEdgeCashflowObjective(
            threshold.applyAsDouble(fishState.getRandom()),
            factory.apply(fishState)
        );

    }

    /**
     * Getter for property 'period'.
     *
     * @return Value for property 'period'.
     */
    public DoubleParameter getPeriod() {
        return period;
    }

    /**
     * Setter for property 'period'.
     *
     * @param period Value to set for property 'period'.
     */
    public void setPeriod(final DoubleParameter period) {
        this.period = period;
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public DoubleParameter getThreshold() {
        return threshold;
    }

    /**
     * Setter for property 'threshold'.
     *
     * @param threshold Value to set for property 'threshold'.
     */
    public void setThreshold(final DoubleParameter threshold) {
        this.threshold = threshold;
    }
}
