/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.complicated.SullivanTransitionAging;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SullivanAgingFactory implements AlgorithmFactory<SullivanTransitionAging> {


    private DoubleParameter LInfinity = new FixedDoubleParameter(113);

    private DoubleParameter K = new FixedDoubleParameter(.364);

    private DoubleParameter gammaScaleParameter = new FixedDoubleParameter(.2);

    private int agingPeriodInDays = 30;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SullivanTransitionAging apply(final FishState fishState) {
        return new SullivanTransitionAging(
            K.applyAsDouble(fishState.getRandom()),
            LInfinity.applyAsDouble(fishState.getRandom()),
            gammaScaleParameter.applyAsDouble(fishState.getRandom()),
            agingPeriodInDays
        );
    }

    /**
     * Getter for property 'LInfinity'.
     *
     * @return Value for property 'LInfinity'.
     */
    public DoubleParameter getLInfinity() {
        return LInfinity;
    }

    /**
     * Setter for property 'LInfinity'.
     *
     * @param LInfinity Value to set for property 'LInfinity'.
     */
    public void setLInfinity(final DoubleParameter LInfinity) {
        this.LInfinity = LInfinity;
    }

    /**
     * Getter for property 'k'.
     *
     * @return Value for property 'k'.
     */
    public DoubleParameter getK() {
        return K;
    }

    /**
     * Setter for property 'k'.
     *
     * @param k Value to set for property 'k'.
     */
    public void setK(final DoubleParameter k) {
        K = k;
    }

    /**
     * Getter for property 'gammaScaleParameter'.
     *
     * @return Value for property 'gammaScaleParameter'.
     */
    public DoubleParameter getGammaScaleParameter() {
        return gammaScaleParameter;
    }

    /**
     * Setter for property 'gammaScaleParameter'.
     *
     * @param gammaScaleParameter Value to set for property 'gammaScaleParameter'.
     */
    public void setGammaScaleParameter(final DoubleParameter gammaScaleParameter) {
        this.gammaScaleParameter = gammaScaleParameter;
    }

    /**
     * Getter for property 'agingPeriodInDays'.
     *
     * @return Value for property 'agingPeriodInDays'.
     */
    public int getAgingPeriodInDays() {
        return agingPeriodInDays;
    }

    /**
     * Setter for property 'agingPeriodInDays'.
     *
     * @param agingPeriodInDays Value to set for property 'agingPeriodInDays'.
     */
    public void setAgingPeriodInDays(final int agingPeriodInDays) {
        this.agingPeriodInDays = agingPeriodInDays;
    }
}
