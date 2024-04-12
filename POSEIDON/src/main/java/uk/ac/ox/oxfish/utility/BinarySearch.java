/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.utility;

import java.util.function.Predicate;

public class BinarySearch {
    private BinarySearch() {
    }

    /**
     * @param max       The maximum number that can be returned.
     * @param predicate The condition to test for each number.
     * @return The highest number that meets the condition.
     */
    public static int highestWhere(
        final int min,
        final int max,
        final Predicate<? super Integer> predicate
    ) {
        int left = min;
        int right = max + 1;
        while (right - left > 1) {
            final int middle = (left + right) / 2;
            if (predicate.test(middle)) {
                left = middle;
            } else {
                right = middle;
            }
        }
        return left; // == -1 ? 0 : left;
    }

}
