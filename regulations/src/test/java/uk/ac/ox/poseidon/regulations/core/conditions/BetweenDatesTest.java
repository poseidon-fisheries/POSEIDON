package uk.ac.ox.poseidon.regulations.core.conditions;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.Month.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BetweenDatesTest {
    @Test
    void test() {
        final BetweenDates betweenDates =
            new BetweenDates(
                LocalDate.of(2017, OCTOBER, 1),
                LocalDate.of(2018, NOVEMBER, 13)
            );
        assertFalse(betweenDates.test(LocalDate.of(2017, SEPTEMBER, 30)));
        assertTrue(betweenDates.test(LocalDate.of(2017, OCTOBER, 1)));
        assertTrue(betweenDates.test(LocalDate.of(2017, OCTOBER, 2)));
        assertTrue(betweenDates.test(LocalDate.of(2018, NOVEMBER, 12)));
        assertTrue(betweenDates.test(LocalDate.of(2018, NOVEMBER, 13)));
        assertFalse(betweenDates.test(LocalDate.of(2018, NOVEMBER, 14)));
    }
}
