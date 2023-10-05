package uk.ac.ox.oxfish.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static uk.ac.ox.oxfish.utility.BinarySearch.highestWhere;

class BinarySearchTest {

    private final int max = 1000;

    @Test
    void highestWhereLessThanOne() {
        Assertions.assertEquals(
            0,
            highestWhere(max, n -> n < 1)
        );
    }

    @Test
    void highestWhereTrue() {
        Assertions.assertEquals(
            max,
            highestWhere(max, n -> true)
        );
    }

    @Test
    void highestWhereLessThan500() {
        Assertions.assertEquals(
            499,
            highestWhere(max, n -> n < 500)
        );
    }
}