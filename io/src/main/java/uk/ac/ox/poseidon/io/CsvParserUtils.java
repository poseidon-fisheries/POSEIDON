/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.io;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Just a bunch of utility methods to facilitate the use of the Univocity CSV parsers.
 */
public class CsvParserUtils {

    private static final CsvParserSettings defaultParserSettings = new CsvParserSettings();
    private static final String defaultDtmFormat = "yyyy-MM-dd'T'HH:mm:ssX";
    private static final ZoneId defaultZoneId = ZoneId.of("UTC");

    static {
        defaultParserSettings.setLineSeparatorDetectionEnabled(true);
        defaultParserSettings.setHeaderExtractionEnabled(true);
        defaultParserSettings.setReadInputOnSeparateThread(false);
    }

    public static List<Record> recordList(final Path inputFilePath) {
        return recordStream(inputFilePath).collect(toImmutableList());
    }

    public static Stream<Record> recordStream(final Path inputFilePath) {
        return recordStream(getReader(inputFilePath));
    }

    public static Stream<Record> recordStream(final Reader reader) {
        final CsvParser csvParser = getCsvParser();
        final ResultIterator<Record, ParsingContext> iterator =
            csvParser.iterateRecords(reader).iterator();
        final Spliterator<Record> spliterator = Spliterators.spliteratorUnknownSize(
            iterator,
            Spliterator.ORDERED
        );
        return StreamSupport.stream(spliterator, false).onClose(csvParser::stopParsing);
    }

    public static Reader getReader(final Path inputFilePath) {
        return getReader(inputFilePath.normalize().toString());
    }

    public static CsvParser getCsvParser() {
        return new CsvParser(defaultParserSettings);
    }

    private static Reader getReader(final String inputFileName) {
        try {
            return new FileReader(inputFileName, UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(
                "Failed to read " + inputFileName + " with exception: " + e
            );
        }
    }

    public static LocalDate getLocalDate(
        final Record record,
        final String headerName
    ) {
        return getLocalDate(record, headerName, defaultDtmFormat, defaultZoneId);
    }

    public static LocalDate getLocalDate(
        final Record record,
        final String headerName,
        final String dtmFormat,
        final ZoneId zoneId
    ) {
        return record.getDate(headerName, dtmFormat).toInstant().atZone(zoneId).toLocalDate();
    }

    public static LocalDate getLocalDate(
        final Record record,
        final String headerName,
        final String dtmFormat
    ) {
        return getLocalDate(record, headerName, dtmFormat, defaultZoneId);
    }

    public static LocalDateTime getLocalDateTime(
        final Record record,
        final String headerName
    ) {
        return getLocalDateTime(record, headerName, defaultDtmFormat, defaultZoneId);
    }

    public static LocalDateTime getLocalDateTime(
        final Record record,
        final String headerName,
        final String dtmFormat,
        final ZoneId zoneId
    ) {
        return record.getDate(headerName, dtmFormat).toInstant().atZone(zoneId).toLocalDateTime();
    }

    public static <T> void writeBeans(
        final Path outputFile,
        final Iterable<T> beans,
        final Class<T> beanClass
    ) {
        try (final OutputStream outputstream = Files.newOutputStream(outputFile)) {
            final CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
            csvWriterSettings.setRowWriterProcessor(new BeanWriterProcessor<>(beanClass));
            final CsvWriter csvWriter = new CsvWriter(outputstream, csvWriterSettings);
            csvWriter.writeHeaders();
            csvWriter.processRecordsAndClose(beans);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <C extends Collection<?>> void writeRows(
        final Path outputFile,
        final Collection<?> headers,
        final Iterable<C> rows
    ) {
        // noinspection ResultOfMethodCallIgnored
        outputFile.getParent().toFile().mkdirs();
        try (final OutputStream outputstream = Files.newOutputStream(outputFile)) {
            final CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
            final CsvWriter csvWriter = new CsvWriter(outputstream, csvWriterSettings);
            csvWriter.writeHeaders(headers);
            csvWriter.writeRowsAndClose(rows);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
