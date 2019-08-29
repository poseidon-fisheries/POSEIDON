package uk.ac.ox.oxfish.geography.currents;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.vividsolutions.jts.geom.Coordinate;
import org.jetbrains.annotations.NotNull;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.MasonUtils;
import uk.ac.ox.oxfish.utility.Pair;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.google.common.base.Predicates.not;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.MasonUtils.coordinateToXY;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.getCsvParser;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.getLocalDate;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.getReader;

public class CurrentsMapFactory {

    private static final Double2D NULL_VECTOR = new Double2D(0, 0);
    private static final int SECONDS_PER_DAY = 60 * 60 * 24;

    public static CurrentMaps makeCurrentsMaps(NauticalMap nauticalMap, Path currentsFilePath) {
        SortedMap<LocalDate, VectorGrid2D> vectorGrids = vectorGrids(nauticalMap, currentsFilePath);
        final ArrayList<VectorGrid2D> gridList = new ArrayList<>(vectorGrids.values());
        final Map<Long, Integer> stepsMap = stepsMap(new ArrayList<>(vectorGrids.keySet()));
        return new CurrentMaps(gridList, steps -> {
            final Integer res = stepsMap.getOrDefault(steps, gridList.size() - 1);
            return res;
        });
    }

    @NotNull private static SortedMap<LocalDate, VectorGrid2D> vectorGrids(
        NauticalMap nauticalMap, Path currentsFilePath
    ) {
        final int width = nauticalMap.getWidth();
        final int height = nauticalMap.getHeight();
        final GeomGridField gridField = nauticalMap.getRasterBathymetry();
        // Create grid fields for each date/time in the currents file and place the raw m/s vectors on them
        SortedMap<LocalDate, VectorGrid2D> vectorGrids = new TreeMap<>();
        final CsvParser csvParser = getCsvParser();
        csvParser.beginParsing(getReader(currentsFilePath));
        Record record;
        LocalDate oldDate = null;
        Continuous2D vectorField = null;
        while ((record = csvParser.parseNextRecord()) != null) {
            final LocalDate newDate = getLocalDate(record, "dttm");
            if (oldDate == null || !newDate.isEqual(oldDate)) {
                if (vectorField != null)
                    vectorGrids.put(oldDate, makeVectorGrid(nauticalMap, vectorField));
                oldDate = newDate;
                vectorField = new Continuous2D(1.0, width, height);
            }
            updateField(vectorField, gridField, record);
        }
        csvParser.stopParsing();
        vectorGrids.put(oldDate, makeVectorGrid(nauticalMap, vectorField));
        return vectorGrids;
    }

    /**
     * Take the range of days inside the range of dates for which we have currents and map each day
     * to the index of the most recent date for which we have currents, relative to that day
     */
    @NotNull private static Map<Long, Integer> stepsMap(List<LocalDate> dates) {
        final LocalDate firstDate = dates.get(0);
        final LocalDate lastDate = dates.get(dates.size() - 1);
        return Stream
            .iterate(firstDate, d -> d.plusDays(1))
            .limit(DAYS.between(firstDate, lastDate) + 1)
            .collect(toMap(
                d -> DAYS.between(firstDate, d),
                d -> dates.stream().filter(not(d::isBefore)).max(LocalDate::compareTo).map(dates::indexOf).get()
            ));
    }

    @NotNull private static VectorGrid2D makeVectorGrid(NauticalMap nauticalMap, Continuous2D vectorField) {

        Map<SeaTile, Double2D> vectorsMap =
            nauticalMap.getAllSeaTilesExcludingLandAsList().stream().map(seaTile -> {
                final Double2D tileCentre = new Double2D(seaTile.getGridX() + 0.5, seaTile.getGridY() + 0.5);
                final Bag vectors = vectorField.getNeighborsExactlyWithinDistance(tileCentre, 1);
                final Double2D vector = Optional.of(vectors)
                    .filter(vs -> vs.size() > 0)
                    .map(vs -> combineVectors(vs, seaTile, nauticalMap))
                    .orElse(NULL_VECTOR);
                return new Pair<>(seaTile, vector);
            }).collect(toMap(Pair::getFirst, Pair::getSecond));

        return new VectorGrid2D(nauticalMap.getWidth(), nauticalMap.getHeight(), vectorsMap);
    }

    private static void updateField(Continuous2D vectorField, GeomGridField gridField, Record record) {
        final Double2D uv = new Double2D(
            record.getDouble("u"),
            record.getDouble("v")
        );
        final Double2D xy = new Double2D(
            gridField.toXCoord(record.getDouble("lon")),
            gridField.toYCoord(record.getDouble("lat"))
        );
        vectorField.setObjectLocation(uv, xy);
    }

    /**
     * Converts a bunch of metres/second vectors at a location into a single, averaged, grid-xy offsets/day vector.
     * This is slightly convoluted because the translation of distance into grid offsets depends on the latitude,
     * so we need to use lon/lat coordinates as an intermediate and then convert back to grid coordinates.
     */
    private static Double2D combineVectors(Bag vectors, SeaTile seaTile, NauticalMap nauticalMap) {
        final GeomGridField gridField = nauticalMap.getRasterBathymetry();
        final Double2D metresVector = MasonUtils.<Double2D>bagToStream(vectors)
            .reduce(NULL_VECTOR, Double2D::add)    // Sum all the m/s vectors at location
            .multiply((1.0 / vectors.size()) * SECONDS_PER_DAY); // Take the average vector, and convert to metres per day
        final Coordinate startCoord = nauticalMap.getCoordinates(seaTile);
        final Double2D startXY = coordinateToXY(gridField, startCoord);
        final Double2D lonLatVector = metresVectorToLonLatVector(startCoord, metresVector.x, metresVector.y);
        final Coordinate endCoord = new Coordinate(startCoord.x + lonLatVector.x, startCoord.y + lonLatVector.y);
        final Double2D endXY = coordinateToXY(gridField, endCoord);
        return endXY.add(startXY.negate());
    }

    /**
     * Takes a vector of offsets in metres and converts it to a vector of longitude/latitude
     * offsets, assuming that we are in the vicinity of {@code coord}. Adapted from
     * https://stackoverflow.com/a/2839560 and https://stackoverflow.com/a/7478827.
     */
    private static Double2D metresVectorToLonLatVector(Coordinate coord, Double u, Double v) {
        double r = EquirectangularDistance.EARTH_RADIUS * 1000; // Earth radius in metres
        final double dx = (180 / Math.PI) * (u / r) / Math.cos(Math.PI / 180.0 * coord.y);
        final double dy = (180 / Math.PI) * (v / r);
        return new Double2D(dx, dy);
    }

}
