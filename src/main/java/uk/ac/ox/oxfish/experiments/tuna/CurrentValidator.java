package uk.ac.ox.oxfish.experiments.tuna;

import static uk.ac.ox.oxfish.model.scenario.TunaScenario.CURRENTS_FILE;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.INPUT_DIRECTORY;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.MAP_FILE;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectMover;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectMoverFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;

public class CurrentValidator {

    public static void main(String[] args) {

        NauticalMap map =
            new FromFileMapInitializer(MAP_FILE, 120, true, true)
                .makeMap(null, null, null);

        DriftingObjectMover driftingObjectMover =
            new DriftingObjectMoverFactory(CURRENTS_FILE).apply(map);

        final Path casesPath = INPUT_DIRECTORY.resolve("fad_trajectories_validation_cases.csv");
        final File outputFile = INPUT_DIRECTORY.resolve("simulated_fad_trajectories.csv").toFile();
        final String dtmFormat = "yyyy-MM-dd'T'HH:mm:ssX";
        CsvWriter writer = new CsvWriter(outputFile, new CsvWriterSettings());
        writer.writeHeaders("segment", "date_time", "lon", "lat");
        for (Record record : parseAllRecords(casesPath)) {
            final String segment = record.getString("segment");
            Coordinate coord = new Coordinate(
                record.getDouble("lon"),
                record.getDouble("lat")
            );
            LocalDate date = record.getDate("start", dtmFormat)
                .toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
            final int duration = record.getInt("duration");
            writer.writeRow(segment, date, coord.x, coord.y);
            for (int day = 0; day < duration; day++) {
                date = date.plusDays(1);
                coord = driftingObjectMover.apply(coord);
                writer.writeRow(segment, date, coord.x, coord.y);
            }
        }
        writer.close();
    }
}