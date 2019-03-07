package uk.ac.ox.oxfish.utility.csv;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

public class CsvParserUtil {

    private static final CsvParserSettings defaultParserSettings = new CsvParserSettings();
    private static final CsvRoutines csvRoutines = new CsvRoutines(defaultParserSettings);

    static {
        defaultParserSettings.setLineSeparatorDetectionEnabled(true);
        defaultParserSettings.setHeaderExtractionEnabled(true);
    }

    public static List<Record> parseAllRecords(Path inputFilePath) {
        return parseAllRecords(getReader(inputFilePath));
    }

    public static List<Record> parseAllRecords(Reader reader) {
        return new CsvParser(defaultParserSettings).parseAllRecords(reader);
    }

    public static Reader getReader(Path inputFilePath) {
        return getReader(inputFilePath.normalize().toString());
    }

    public static Reader getReader(String inputFileName) {
        try {
            return new FileReader(inputFileName);
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to read " + inputFileName + " with exception: " + e
            );
        }
    }

    public static <T> List<T> parseAll(final Class<T> beanType, final Path inputFilePath) {
        return csvRoutines.parseAll(beanType, getReader(inputFilePath));
    }
}
