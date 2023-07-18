package uk.ac.ox.poseidon.regulations.core;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;

import static java.time.Month.*;

public class BetweenYearlyDatesTest {

    @Test
    public void testWhenNotYearSpanning() {
        final BetweenYearlyDates betweenYearlyDates =
            new BetweenYearlyDates(
                MonthDay.of(OCTOBER, 1),
                MonthDay.of(NOVEMBER, 13)
            );
        Assert.assertFalse(betweenYearlyDates.isYearSpanning());
        Assert.assertFalse(betweenYearlyDates.test(MonthDay.of(SEPTEMBER, 30)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(OCTOBER, 1)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(OCTOBER, 2)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 12)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 13)));
        Assert.assertFalse(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 14)));
    }

    @Test
    public void testWhenYearSpanning() {
        final BetweenYearlyDates betweenYearlyDates =
            new BetweenYearlyDates(
                MonthDay.of(DECEMBER, 15),
                MonthDay.of(JANUARY, 15)
            );
        Assert.assertTrue(betweenYearlyDates.isYearSpanning());
        Assert.assertFalse(betweenYearlyDates.test(MonthDay.of(DECEMBER, 14)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(DECEMBER, 15)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(DECEMBER, 16)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(JANUARY, 14)));
        Assert.assertTrue(betweenYearlyDates.test(MonthDay.of(JANUARY, 15)));
        Assert.assertFalse(betweenYearlyDates.test(MonthDay.of(JANUARY, 16)));
    }

}