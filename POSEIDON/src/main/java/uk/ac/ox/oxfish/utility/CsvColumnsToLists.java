/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.utility;

import com.google.common.base.Preconditions;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/**
 * utility to read multiple columnNames of a csv file and turn them into multiple lists of doubles
 * This utility differs from CSVColumnToList for being heading based.
 * <p>
 * It assumes " is used as a quoting character Created by carrknight on 11/30/16.
 */
public class CsvColumnsToLists {

    /**
     * path to file
     */
    private final String pathToCSV;

    /**
     * csv separator
     */
    private final char separator;

    /**
     * columnNames to read
     */
    private final String[] columnNames;

    public CsvColumnsToLists(
        final String pathToCSV,
        final char separator,
        final String[] columnNames
    ) {
        this.pathToCSV = pathToCSV;
        this.separator = separator;
        this.columnNames = columnNames;
        Preconditions.checkArgument(columnNames.length >= 1, "no columnNames given to read");
        // trim them all
        for (int i = 0; i < columnNames.length; i++)
            columnNames[i] = standardizeColumnName(columnNames[i]);
    }

    private static String standardizeColumnName(final String columnName) {
        // I think it's a terrible idea to do this, but we're keeping the behaviour
        // in place until we can get rid of this class entirely
        return columnName.replace("\"", "").trim().toLowerCase();
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Double>[] readColumns() {
        try (final Reader reader = new FileReader(pathToCSV)) {
            final CsvParserSettings settings = new CsvParserSettings();
            settings.setHeaderExtractionEnabled(true);
            settings.getFormat().setDelimiter(separator);
            final CsvParser csvParser = new CsvParser(settings);
            final List<String[]> rows = csvParser.parseAll(reader);
            final String[] headers = csvParser.getRecordMetadata().headers();
            return Arrays.stream(columnNames)
                .mapToInt(columnName ->
                    IntStream.range(0, headers.length)
                        .filter(i -> standardizeColumnName(headers[i]).equals(columnName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                            MessageFormat.format(
                                "Failed to find column {0} in the heading: {1}",
                                columnName,
                                Arrays.toString(headers)
                            )
                        ))
                )
                .mapToObj(columnIndex ->
                    rows.stream()
                        .map(row -> row[columnIndex])
                        .map(Double::parseDouble)
                        .collect(toCollection(LinkedList<Double>::new))
                )
                .toArray(LinkedList[]::new);
        } catch (final IOException e) {
            throw new RuntimeException(MessageFormat.format(
                "failed to read or parse {0} with exception {1}",
                pathToCSV,
                e
            ));
        }
    }

}
