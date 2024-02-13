package uk.ac.ox.poseidon.common.core.temporal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.MonthDay;

import static java.time.Year.isLeap;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ByDateKeyedByMonthDayTest {

    private final LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);

    @Test
    void getAtStep() {
        final TemporalMap<String> byMonthDay =
            new NavigableTemporalMap<>(
                ImmutableSortedMap.of(
                    toMonthDay(0), "A",
                    toMonthDay(100), "B",
                    toMonthDay(364), "C"
                ),
                MonthDay::from
            );
        ImmutableMap.of(
            0, "A",
            1, "A",
            99, "A",
            100, "B",
            101, "B",
            363, "B",
            364, "C",
            365, isLeap(startDate.getYear()) ? "C" : "A",
            366, "A"
        ).forEach((step, expected) ->
            assertEquals(
                expected,
                byMonthDay.get(startDate.plusDays(step)),
                () -> "at step " + step + " which is " + startDate.plusDays(step)
            )
        );
    }

    private MonthDay toMonthDay(final int daysToAdd) {
        return MonthDay.from(startDate.plusDays(daysToAdd));
    }
}
