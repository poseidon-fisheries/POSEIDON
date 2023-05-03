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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.OneBinInitialAbundance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class OneBinAbundanceFactory implements AlgorithmFactory<OneBinInitialAbundance> {


    private int initialBin = 0;

    private int initialSubdivision = -1;

    private DoubleParameter initialBinPopulation = new FixedDoubleParameter(10000d);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OneBinInitialAbundance apply(final FishState fishState) {
        return new OneBinInitialAbundance(
            initialBin,
            initialBinPopulation.applyAsDouble(fishState.getRandom()),
            initialSubdivision
        );
    }

    /**
     * Getter for property 'initialBin'.
     *
     * @return Value for property 'initialBin'.
     */
    public int getInitialBin() {
        return initialBin;
    }

    /**
     * Setter for property 'initialBin'.
     *
     * @param initialBin Value to set for property 'initialBin'.
     */
    public void setInitialBin(final int initialBin) {
        this.initialBin = initialBin;
    }

    /**
     * Getter for property 'initialSubdivision'.
     *
     * @return Value for property 'initialSubdivision'.
     */
    public int getInitialSubdivision() {
        return initialSubdivision;
    }

    /**
     * Setter for property 'initialSubdivision'.
     *
     * @param initialSubdivision Value to set for property 'initialSubdivision'.
     */
    public void setInitialSubdivision(final int initialSubdivision) {
        this.initialSubdivision = initialSubdivision;
    }

    /**
     * Getter for property 'initialBinPopulation'.
     *
     * @return Value for property 'initialBinPopulation'.
     */
    public DoubleParameter getInitialBinPopulation() {
        return initialBinPopulation;
    }

    /**
     * Setter for property 'initialBinPopulation'.
     *
     * @param initialBinPopulation Value to set for property 'initialBinPopulation'.
     */
    public void setInitialBinPopulation(final DoubleParameter initialBinPopulation) {
        this.initialBinPopulation = initialBinPopulation;
    }
}
