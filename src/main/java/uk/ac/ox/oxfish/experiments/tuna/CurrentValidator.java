package uk.ac.ox.oxfish.experiments.tuna;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.vividsolutions.jts.geom.Coordinate;
import sim.field.geo.GeomGridField;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentMaps;
import uk.ac.ox.oxfish.geography.currents.VectorGrid2D;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializer;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static uk.ac.ox.oxfish.geography.currents.CurrentsMapFactory.makeCurrentsMaps;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.INPUT_DIRECTORY;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.MAP_FILE;
import static uk.ac.ox.oxfish.utility.MasonUtils.coordinateToXY;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.getLocalDateTime;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class CurrentValidator {

    public static void main(String[] args) {

        NauticalMap map =
            new FromFileMapInitializer(MAP_FILE, 120, 0.5, true, true)
                .makeMap(null, null, null);

        final Path casesPath = INPUT_DIRECTORY.resolve("fad_trajectories_validation_cases.csv");
        Stream.of(/*"hycom_2016", "seapodym_2010",*/ "seapodym_2016_1", "seapodym_2016_2").forEach(source -> {
            final Path currentsPath = INPUT_DIRECTORY.resolve("currents_" + source + ".csv");
            final Path outputFile = INPUT_DIRECTORY.resolve("simulated_fad_trajectories_" + source + ".csv");
            generateTrajectories(map, casesPath, currentsPath, outputFile);
        });
    }

    private static void generateTrajectories(
        NauticalMap nauticalMap, Path casesPath, Path currentsPath, Path outputPath
    ) {
        GeomGridField gridField = nauticalMap.getRasterBathymetry();
        final CurrentMaps currentsMaps = makeCurrentsMaps(nauticalMap, currentsPath);
        CsvWriter writer = new CsvWriter(outputPath.toFile(), new CsvWriterSettings());
        writer.writeHeaders("trajectory", "date_time", "lon", "lat");
        final List<Record> records = parseAllRecords(casesPath);
        final LocalDateTime firstDate = records.stream().findFirst()
            .map(r -> getLocalDateTime(r, "start")).get();
        for (Record record : records) {
            final String trajectory = record.getString("trajectory");
            Coordinate coord = new Coordinate(
                record.getDouble("lon"),
                record.getDouble("lat")
            );
            LocalDateTime date = getLocalDateTime(record, "start");
            final long startSteps = DAYS.between(firstDate, date);
            final int duration = record.getInt("duration");
            writer.writeRow(trajectory, date, coord.x, coord.y);
            Double2D xy = coordinateToXY(gridField, coord);
            for (int day = 0; day < duration; day++) {
                final VectorGrid2D currentsMap = currentsMaps.atSteps(startSteps + day);
                date = date.plusDays(1);
                final Optional<Double2D> newXY = currentsMap.move(xy);
                if (newXY.isPresent()) {
                    xy = newXY.get();
                    coord = gridField.toPoint((int) xy.x, (int) xy.y).getCoordinate();
                    writer.writeRow(trajectory, date, coord.x, coord.y);
                }
            }
        }
        writer.close();
    }
}