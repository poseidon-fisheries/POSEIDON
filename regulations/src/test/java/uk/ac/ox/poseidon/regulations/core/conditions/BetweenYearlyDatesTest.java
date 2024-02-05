package uk.ac.ox.poseidon.regulations.core.conditions;

import org.junit.jupiter.api.Test;

import java.time.MonthDay;

import static java.time.Month.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetweenYearlyDatesTest {

    @Test
    void whenNotYearSpanning() {
        final BetweenYearlyDates betweenYearlyDates =
            new BetweenYearlyDates(
                MonthDay.of(OCTOBER, 1),
                MonthDay.of(NOVEMBER, 13)
            );
        assertFalse(betweenYearlyDates.isYearSpanning());
        assertFalse(betweenYearlyDates.test(MonthDay.of(SEPTEMBER, 30)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(OCTOBER, 1)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(OCTOBER, 2)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 12)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 13)));
        assertFalse(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 14)));
    }

    @Test
    void whenYearSpanning() {
        final BetweenYearlyDates betweenYearlyDates =
            new BetweenYearlyDates(
                MonthDay.of(DECEMBER, 15),
                MonthDay.of(JANUARY, 15)
            );
        assertTrue(betweenYearlyDates.isYearSpanning());
        assertFalse(betweenYearlyDates.test(MonthDay.of(DECEMBER, 14)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(DECEMBER, 15)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(DECEMBER, 16)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(JANUARY, 14)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(JANUARY, 15)));
        assertFalse(betweenYearlyDates.test(MonthDay.of(JANUARY, 16)));
    }

}
