package uk.ac.ox.oxfish.geography.currents;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.univocity.parsers.common.record.Record;
import com.vividsolutions.jts.geom.Coordinate;
import sim.field.geo.GeomGridField;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.utility.csv.CsvParserUtil;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.MasonUtils.coordinateToXY;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.getLocalDate;

public enum CurrentVectorsFactory {

    INSTANCE;

    public static final int STEPS_PER_DAY = 1;
    private static final int SECONDS_PER_DAY = 60 * 60 * 24;

    private final LoadingCache<Entry<MapExtent, Map<CurrentPattern, Path>>, CurrentVectors> cache =
        CacheBuilder.newBuilder()
            .build(CacheLoader.from(entry -> {
                MapExtent mapExtent = entry.getKey();
                Map<CurrentPattern, Path> currentFiles = entry.getValue();
                return new CurrentVectors(
                    makeVectorMaps(mapExtent, currentFiles),
                    STEPS_PER_DAY,
                    mapExtent.getGridWidth(),
                    mapExtent.getGridHeight()
                );
            }));


    public CurrentVectors getCurrentVectors(MapExtent mapExtent, Map<CurrentPattern, Path> currentFiles) {
        return cache.getUnchecked(entry(mapExtent, currentFiles));
    }

    @SuppressWarnings("SameParameterValue")
    private TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> makeVectorMaps(
        MapExtent mapExtent,
        Map<CurrentPattern, Path> currentFiles
    ) {
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> currentVectors = new TreeMap<>();
        final SparseGrid2D dummyGrid = new SparseGrid2D(mapExtent.getGridWidth(), mapExtent.getGridHeight());
        final GeomGridField geomGridField = new GeomGridField(dummyGrid);
        geomGridField.setMBR(mapExtent.getEnvelope());

        currentFiles.forEach((currentPattern, path) ->
            CsvParserUtil.parseAllRecords(path).forEach(record -> {
                final Coordinate coordinate = readCoordinate(record);
                final int dayOfYear = getLocalDate(record, "dttm").getDayOfYear();
                final Map<Int2D, Double2D> vectorByLocation = currentVectors
                    .computeIfAbsent(dayOfYear, __ -> new EnumMap<>(CurrentPattern.class))
                    .computeIfAbsent(currentPattern, __ -> new HashMap<>());
                final Double2D vector = readVector(record, coordinate, mapExtent);
                final Int2D gridLocation = new Int2D(
                    geomGridField.toXCoord(coordinate.x),
                    geomGridField.toYCoord(coordinate.y)
                );
                // mutates the innermost map in the main data structure
                vectorByLocation.put(gridLocation, vector);
            }));
        return currentVectors;
    }

    private Coordinate readCoordinate(Record record) {
        return new Coordinate(record.getDouble("lon"), record.getDouble("lat"));
    }

    private Double2D readVector(Record record, Coordinate startCoord, MapExtent mapExtent) {
        final Double2D metrePerSecondVector = new Double2D(
            record.getDouble("u"),
            record.getDouble("v")
        );
        return metrePerSecondToXyPerDaysVector(metrePerSecondVector, startCoord, mapExtent);
    }

    /**
     * Converts a metres/second vector at a location into a grid-xy offsets/day vector.
     * This is slightly convoluted because the translation of distance into grid offsets depends on the latitude,
     * so we need to use lon/lat coordinates as an intermediate and then convert back to grid coordinates.
     */
    private Double2D metrePerSecondToXyPerDaysVector(Double2D metrePerSecondVector, Coordinate startCoord, MapExtent mapExtent) {
        final Double2D metresPerDayVector = metrePerSecondVector.multiply(SECONDS_PER_DAY);
        final Double2D startXY = coordinateToXY(startCoord, mapExtent);
        final Double2D lonLatVector = metresVectorToLonLatVector(startCoord, metresPerDayVector.x, metresPerDayVector.y);
        final Coordinate endCoord = new Coordinate(startCoord.x + lonLatVector.x, startCoord.y + lonLatVector.y);
        final Double2D endXY = coordinateToXY(endCoord, mapExtent);
        return endXY.add(startXY.negate());
    }

    /**
     * Takes a vector of offsets in metres and converts it to a vector of longitude/latitude
     * offsets, assuming that we are in the vicinity of {@code coord}. Adapted from
     * https://stackoverflow.com/a/2839560 and https://stackoverflow.com/a/7478827.
     */
    private Double2D metresVectorToLonLatVector(Coordinate coord, Double u, Double v) {
        double r = EquirectangularDistance.EARTH_RADIUS * 1000; // Earth radius in metres
        final double dx = (180 / Math.PI) * (u / r) / Math.cos(Math.PI / 180.0 * coord.y);
        final double dy = (180 / Math.PI) * (v / r);
        return new Double2D(dx, dy);
    }

}
