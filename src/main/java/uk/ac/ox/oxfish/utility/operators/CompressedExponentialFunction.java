/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility.operators;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.exp;
import static java.lang.Math.pow;

import java.util.function.DoubleUnaryOperator;

public class CompressedExponentialFunction implements DoubleUnaryOperator {

    private final double coefficient;
    private final double exponent;

    public CompressedExponentialFunction(final double coefficient, final double exponent) {

        checkArgument(
            coefficient >= 0 && coefficient <= 1,
            "coefficient was %s", coefficient
        );

        // exponent < 1 would be a "stretched" exponential; a legit thing, but not what we want here
        checkArgument(
            exponent >= 1,
            "exponent was %s", exponent
        );

        this.coefficient = coefficient;
        this.exponent = exponent;
    }

    @Override
    public double applyAsDouble(final double v) {
        final double x = pow(v * coefficient, exponent);
        return Double.isNaN(x)
            ? 0 // very small values of (v * coefficient) can produce NaN, so we return 0
            : 1 - exp(-x);
    }
}