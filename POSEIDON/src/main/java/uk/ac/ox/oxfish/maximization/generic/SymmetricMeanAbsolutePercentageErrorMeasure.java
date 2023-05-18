/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.maximization.generic;

import static java.lang.Math.abs;

/**
 * Implements the SMAPE (Symmetric mean absolute percentage error) measure.
 * <p>
 * This is the version of the measure where the denominator is not divided by two,
 * thus producing a result between 0% and 100%.
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/Symmetric_mean_absolute_percentage_error">https://en.wikipedia.org/wiki/Symmetric_mean_absolute_percentage_error</a>
 */
public enum SymmetricMeanAbsolutePercentageErrorMeasure implements ForecastErrorMeasure {

    INSTANCE;

    @Override
    public double applyAsDouble(final double actualValue, final double predictedValue) {
        return (actualValue == predictedValue)
            ? 0 // avoids NaN when both actual and predicted are 0
            : 100 * abs(actualValue - predictedValue) / (abs(predictedValue) + abs(actualValue));
    }

}
