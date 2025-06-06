/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
import uk.ac.ox.poseidon.common.core.csv.CsvParserUtil;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.getLocalDate;

public enum CurrentVectorsFactory {

    INSTANCE;

    public static final int STEPS_PER_DAY = 1;
    public static final int SECONDS_PER_DAY = 60 * 60 * 24;

    private final LoadingCache<Entry<MapExtent, Entry<Boolean, Map<CurrentPattern, Path>>>, CurrentVectors> cache =
        CacheBuilder.newBuilder().maximumSize(1).build(CacheLoader.from(entry -> {
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

    @SuppressWarnings("SameParameterValue")
    private static TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> makeVectorMaps(
        final MapExtent mapExtent,
        final Map<CurrentPattern, ? extends Path> currentFiles,
        final boolean inputIsMetersPerSecond1
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

    private static Double2D readVector(
        final Record record,
        final Coordinate startCoord,
        final MapExtent mapExtent,
        final boolean inputIsMetersPerSecond1
    ) {
        final Double2D input = new Double2D(
            record.getDouble("u"),
            record.getDouble("v")
        );
        if (inputIsMetersPerSecond1) {
            return metrePerSecondToXyPerDaysVector(input, startCoord, mapExtent);
        } else {
            return input;
        }
    }

    /**
     * Converts a metres/second vector at a location into a grid-xy offsets/day vector. This is slightly convoluted
     * because the translation of distance into grid offsets depends on the latitude, so we need to use lon/lat
     * coordinates as an intermediate and then convert back to grid coordinates.
     */
    public static Double2D metrePerSecondToXyPerDaysVector(
        final Double2D metrePerSecondVector,
        final Coordinate startCoord,
        final MapExtent mapExtent
    ) {
        final Double2D metresPerDayVector = metrePerSecondVector.multiply(SECONDS_PER_DAY);
        final Double2D startXY = mapExtent.coordinateToXY(startCoord);
        final Double2D lonLatVector = metresVectorToLonLatVector(
            startCoord,
            metresPerDayVector.x,
            metresPerDayVector.y
        );
        final Coordinate endCoord = new Coordinate(startCoord.x + lonLatVector.x, startCoord.y + lonLatVector.y);
        final Double2D endXY = mapExtent.coordinateToXY(endCoord);
        return endXY.add(startXY.negate());
    }

    /**
     * Takes a vector of offsets in metres and converts it to a vector of longitude/latitude offsets, assuming that we
     * are in the vicinity of {@code coord}. Adapted from
     * <a href="https://stackoverflow.com/a/2839560">https://stackoverflow.com/a/2839560</a> and
     * <a href="https://stackoverflow.com/a/7478827">https://stackoverflow.com/a/7478827</a>.
     */
    private static Double2D metresVectorToLonLatVector(
        final Coordinate coord,
        final Double u,
        final Double v
    ) {
        final double r = EquirectangularDistance.EARTH_RADIUS * 1000; // Earth radius in metres
        final double dx = (180 / Math.PI) * (u / r) / Math.cos(Math.PI / 180.0 * coord.y);
        final double dy = (180 / Math.PI) * (v / r);
        return new Double2D(dx, dy);
    }

    public CurrentVectors getCurrentVectors(
        final MapExtent mapExtent,
        final Map<CurrentPattern, Path> currentFiles,
        final boolean inputIsMetersPerSecond
    ) {
        return cache.getUnchecked(entry(mapExtent, entry(inputIsMetersPerSecond, currentFiles)));
    }

}
