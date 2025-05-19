/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class MaxHoursOutFactory implements AlgorithmFactory<MaxHoursOutRegulation> {


    private AlgorithmFactory<? extends Regulation> delegate = new AnarchyFactory();


    private DoubleParameter maxHoursOut = new FixedDoubleParameter(1000);


    public MaxHoursOutFactory() {
    }


    public MaxHoursOutFactory(final double maxHoursOut) {
        this.maxHoursOut = new FixedDoubleParameter(maxHoursOut);
    }


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MaxHoursOutRegulation apply(final FishState fishState) {
        return new MaxHoursOutRegulation(
            delegate.apply(fishState),
            maxHoursOut.applyAsDouble(fishState.getRandom())
        );
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends Regulation> getDelegate() {
        return delegate;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param delegate Value to set for property 'delegate'.
     */
    public void setDelegate(final AlgorithmFactory<? extends Regulation> delegate) {
        this.delegate = delegate;
    }

    /**
     * Getter for property 'maxHoursOut'.
     *
     * @return Value for property 'maxHoursOut'.
     */
    public DoubleParameter getMaxHoursOut() {
        return maxHoursOut;
    }

    /**
     * Setter for property 'maxHoursOut'.
     *
     * @param maxHoursOut Value to set for property 'maxHoursOut'.
     */
    public void setMaxHoursOut(final DoubleParameter maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }
}
