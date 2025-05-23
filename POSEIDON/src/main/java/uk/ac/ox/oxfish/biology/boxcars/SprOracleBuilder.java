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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SprOracleBuilder implements AlgorithmFactory<SprOracle> {

    private int dayOfMeasurement = 365;

    private DoubleParameter lengthAtMaturity = new FixedDoubleParameter(50);

    private DoubleParameter virginSSB = new FixedDoubleParameter(201231231);

    private String speciesName = "Species 0";


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SprOracle apply(final FishState fishState) {
        return new SprOracle(
            fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName),
            lengthAtMaturity.applyAsDouble(fishState.getRandom()),
            dayOfMeasurement,
            virginSSB.applyAsDouble(fishState.getRandom())
        );
    }


    /**
     * Getter for property 'dayOfMeasurement'.
     *
     * @return Value for property 'dayOfMeasurement'.
     */
    public int getDayOfMeasurement() {
        return dayOfMeasurement;
    }

    /**
     * Setter for property 'dayOfMeasurement'.
     *
     * @param dayOfMeasurement Value to set for property 'dayOfMeasurement'.
     */
    public void setDayOfMeasurement(final int dayOfMeasurement) {
        this.dayOfMeasurement = dayOfMeasurement;
    }

    /**
     * Getter for property 'lengthAtMaturity'.
     *
     * @return Value for property 'lengthAtMaturity'.
     */
    public DoubleParameter getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    /**
     * Setter for property 'lengthAtMaturity'.
     *
     * @param lengthAtMaturity Value to set for property 'lengthAtMaturity'.
     */
    public void setLengthAtMaturity(final DoubleParameter lengthAtMaturity) {
        this.lengthAtMaturity = lengthAtMaturity;
    }

    /**
     * Getter for property 'virginSSB'.
     *
     * @return Value for property 'virginSSB'.
     */
    public DoubleParameter getVirginSSB() {
        return virginSSB;
    }

    /**
     * Setter for property 'virginSSB'.
     *
     * @param virginSSB Value to set for property 'virginSSB'.
     */
    public void setVirginSSB(final DoubleParameter virginSSB) {
        this.virginSSB = virginSSB;
    }

    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
    }
}
