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
    public static final int SECONDS_PER_DAY = 60 * 60 * 24;

    private final LoadingCache<Entry<MapExtent, Entry<Boolean, Map<CurrentPattern, Path>>>, CurrentVectors> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(entry -> {
            MapExtent mapExtent = entry.getKey();
            boolean inputIsMetersPerSecond = entry.getValue().getKey();
            Map<CurrentPattern, Path> currentFiles = entry.getValue().getValue();
            return new CurrentVectorsEPO(
                makeVectorMaps(mapExtent, currentFiles, inputIsMetersPerSecond),
                STEPS_PER_DAY,
                mapExtent.getGridWidth(),
                mapExtent.getGridHeight()
            );
        }));

    public CurrentVectors getCurrentVectors(
        final MapExtent mapExtent,
        final Map<CurrentPattern, Path> currentFiles,
        boolean inputIsMetersPerSecond
    ) {
        return cache.getUnchecked(entry(mapExtent, entry(inputIsMetersPerSecond, currentFiles)));
    }

    @SuppressWarnings("SameParameterValue")
    private static TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> makeVectorMaps(
            final MapExtent mapExtent,
            final Map<CurrentPattern, Path> currentFiles, boolean inputIsMetersPerSecond1
    ) {
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> currentVectors = new TreeMap<>();
        final SparseGrid2D dummyGrid = new SparseGrid2D(mapExtent.getGridWidth(), mapExtent.getGridHeight());
        final GeomGridField geomGridField = new GeomGridField(dummyGrid);
        geomGridField.setMBR(mapExtent.getEnvelope());

        currentFiles.forEach((currentPattern, path) ->
            CsvParserUtil.recordStream(path).forEach(record -> {
                final Coordinate coordinate = readCoordinate(record);
                final int dayOfYear = getLocalDate(record, "dttm", "yyyy-MM-dd").getDayOfYear();
                final Map<Int2D, Double2D> vectorByLocation = currentVectors
                    .computeIfAbsent(dayOfYear, __ -> new EnumMap<>(CurrentPattern.class))
                    .computeIfAbsent(currentPattern, __ -> new HashMap<>());
                final Double2D vector = readVector(record, coordinate, mapExtent, inputIsMetersPerSecond1);
                final Int2D gridLocation = new Int2D(
                    geomGridField.toXCoord(coordinate.x),
                    geomGridField.toYCoord(coordinate.y)
                );
                // mutates the innermost map in the main data structure
                vectorByLocation.put(gridLocation, vector);
            }));
        return currentVectors;
    }

    private static Coordinate readCoordinate(final Record record) {
        return new Coordinate(record.getDouble("lon"), record.getDouble("lat"));
    }

    private static Double2D readVector(final Record record, final Coordinate startCoord, final MapExtent mapExtent, boolean inputIsMetersPerSecond1) {
        final Double2D input = new Double2D(
            record.getDouble("u"),
            record.getDouble("v")
        );
        if(inputIsMetersPerSecond1){
        return metrePerSecondToXyPerDaysVector(input, startCoord, mapExtent);}
        else{
            return input;
        }
    }

    /**
     * Converts a metres/second vector at a location into a grid-xy offsets/day vector.
     * This is slightly convoluted because the translation of distance into grid offsets depends on the latitude,
     * so we need to use lon/lat coordinates as an intermediate and then convert back to grid coordinates.
     */
    public static Double2D metrePerSecondToXyPerDaysVector(final Double2D metrePerSecondVector, final Coordinate startCoord, final MapExtent mapExtent) {
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
    private static Double2D metresVectorToLonLatVector(final Coordinate coord, final Double u, final Double v) {
        final double r = EquirectangularDistance.EARTH_RADIUS * 1000; // Earth radius in metres
        final double dx = (180 / Math.PI) * (u / r) / Math.cos(Math.PI / 180.0 * coord.y);
        final double dy = (180 / Math.PI) * (v / r);
        return new Double2D(dx, dy);
    }



}
