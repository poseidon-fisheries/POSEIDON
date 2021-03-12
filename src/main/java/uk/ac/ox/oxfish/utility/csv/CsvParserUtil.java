package uk.ac.ox.oxfish.utility.csv;

import com.univocity.parsers.common.AbstractParser;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;

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

    public static List<Record> parseAllRecords(Path inputFilePath) {
        return parse(inputFilePath, AbstractParser::parseAllRecords);
    }

    private static <T> T parse(Path inputFilePath, BiFunction<CsvParser, Reader, T> parseFunction) {
        final CsvParser csvParser = getCsvParser();
        T result = parseFunction.apply(csvParser, getReader(inputFilePath));
        csvParser.stopParsing();
        return result;
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
