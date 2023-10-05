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
        final int max,
        final Predicate<? super Integer> predicate
    ) {
        int left = -1;
        int right = max + 1;
        while (right - left > 1) {
            final int middle = (left + right) / 2;
            if (predicate.test(middle)) {
                left = middle;
            } else {
                right = middle;
            }
        }
        return left == -1 ? max : left;
    }

}
