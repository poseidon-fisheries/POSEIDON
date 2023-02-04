package uk.ac.ox.oxfish.utility.csv;

import com.univocity.parsers.common.AbstractParser;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Just a bunch of utility methods to facilitate the use of the Univocity CSV parsers.
 */
public class CsvParserUtil {

    private static final CsvParserSettings defaultParserSettings = new CsvParserSettings();
    private static final String defaultDtmFormat = "yyyy-MM-dd'T'HH:mm:ssX";
    private static final ZoneId defaultZoneId = ZoneId.of("UTC");

    static {
        defaultParserSettings.setLineSeparatorDetectionEnabled(true);
        defaultParserSettings.setHeaderExtractionEnabled(true);
        defaultParserSettings.setReadInputOnSeparateThread(false);
    }

    public static Stream<Record> recordStream(Path inputFilePath) {
        final CsvParser csvParser = getCsvParser();
        final Reader reader = getReader(inputFilePath);
        final ResultIterator<Record, ParsingContext> iterator = csvParser.iterateRecords(reader).iterator();
        final Spliterator<Record> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).onClose(csvParser::stopParsing);
    }

    public static CsvParser getCsvParser() {
        return new CsvParser(defaultParserSettings);
    }

    public static Reader getReader(Path inputFilePath) {
        return getReader(inputFilePath.normalize().toString());
    }

    private static Reader getReader(String inputFileName) {
        try {
            return new FileReader(inputFileName);
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to read " + inputFileName + " with exception: " + e
            );
        }
    }

    public static LocalDate getLocalDate(Record record, String headerName) {
        return getLocalDate(record, headerName, defaultDtmFormat, defaultZoneId);
    }

    public static LocalDate getLocalDate(Record record, String headerName, String dtmFormat) {
        return getLocalDate(record, headerName, dtmFormat, defaultZoneId);
    }

    public static LocalDate getLocalDate(Record record, String headerName, String dtmFormat, ZoneId zoneId) {
        return record.getDate(headerName, dtmFormat).toInstant().atZone(zoneId).toLocalDate();
    }

    public static LocalDateTime getLocalDateTime(Record record, String headerName) {
        return getLocalDateTime(record, headerName, defaultDtmFormat, defaultZoneId);
    }

    public static LocalDateTime getLocalDateTime(Record record, String headerName, String dtmFormat, ZoneId zoneId) {
        return record.getDate(headerName, dtmFormat).toInstant().atZone(zoneId).toLocalDateTime();
    }
}
