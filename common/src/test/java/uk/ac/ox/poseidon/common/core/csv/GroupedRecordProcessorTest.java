/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 */

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
