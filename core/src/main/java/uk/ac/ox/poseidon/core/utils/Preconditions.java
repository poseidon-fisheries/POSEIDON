/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.utils;

import static com.google.common.base.Preconditions.checkArgument;

public class Preconditions {
    public static double checkUnitRange(
        final double value,
        final String name
    ) {
        checkArgument(
            value >= 0 && value <= 1,
            "%s must be between 0 and 1 but was %s",
            name,
            value
        );
        return value;
    }

    public static int checkNonNegative(
        final int value,
        final String name
    ) {
        checkArgument(
            value >= 0,
            "%s must be not be negative but was %s",
            name,
            value
        );
        return value;
    }

    public static double checkNonNegative(
        final double value,
        final String name
    ) {
        checkArgument(
            value >= 0,
            "%s must be not be negative but was %s",
            name,
            value
        );
        return value;
    }

    public static double checkPositive(
        final double value,
        final String name
    ) {
        checkArgument(
            value > 0,
            "%s must be positive but was %s",
            name,
            value
        );
        return value;
    }

}
