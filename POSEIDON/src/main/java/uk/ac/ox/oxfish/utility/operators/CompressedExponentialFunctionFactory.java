/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.operators;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class CompressedExponentialFunctionFactory
    implements AlgorithmFactory<CompressedExponentialFunction> {

    private double coefficient;
    private double exponent;

    @SuppressWarnings("unused")
    public CompressedExponentialFunctionFactory() {
    }

    public CompressedExponentialFunctionFactory(final double coefficient) {
        this(coefficient, 2.0);
    }

    public CompressedExponentialFunctionFactory(final double coefficient, final double exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(final double coefficient) {
        this.coefficient = coefficient;
    }

    public double getExponent() {
        return exponent;
    }

    public void setExponent(final double exponent) {
        this.exponent = exponent;
    }

    @Override
    public CompressedExponentialFunction apply(final FishState fishState) {
        return new CompressedExponentialFunction(coefficient, exponent);
    }
}
