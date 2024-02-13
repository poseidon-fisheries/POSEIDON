package uk.ac.ox.poseidon.common.core.csv;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.common.core.temporal.NavigableTemporalMap;
import uk.ac.ox.poseidon.common.core.temporal.TemporalMap;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.MonthDay;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

class GroupedRecordProcessorTest {

    @Test
    void test() {

        final GroupedRecordProcessor<MonthDay, String> groupedRecordProcessor =
            new GroupedRecordProcessor<>(
                "date",
                s -> MonthDay.from(LocalDate.parse(s)),
                recordStream -> recordStream
                    .map(record ->
                        record.getString("x") + record.getString("y")
                    )
                    .collect(joining("-"))
            );

        final TemporalMap<String> temporalMap =
            new NavigableTemporalMap<>(
                groupedRecordProcessor
                    .apply(recordStream(new StringReader(
                        "date,x,y\n" +
                            "2000-01-01,a,b\n" +
                            "2000-01-01,c,d\n" +
                            "2000-03-01,i,j\n" +
                            "2000-02-01,e,f\n" +
                            "2000-02-01,g,h\n"
                    ))),
                MonthDay::from
            );

        final LocalDate startDate = LocalDate.of(1999, 1, 1);
        ImmutableMap.of(
            0, "ab-cd",
            30, "ab-cd",
            31, "ef-gh",
            60, "ij",
            365, "ab-cd"
        ).forEach((step, expected) ->
            assertEquals(expected, temporalMap.get(startDate.plusDays(step)))
        );
    }
}
