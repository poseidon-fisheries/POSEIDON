package uk.ac.ox.oxfish.geography.fads;

import static uk.ac.ox.oxfish.geography.fads.DriftingObjectMover.coordinatesToXY;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAll;

import com.univocity.parsers.annotations.Parsed;
import com.vividsolutions.jts.geom.Coordinate;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.MasonUtils;
import uk.ac.ox.oxfish.utility.Pair;

public class DriftingObjectMoverFactory implements AlgorithmFactory<DriftingObjectMover> {

    private Path currentsVectorFilePath;

    public DriftingObjectMoverFactory(Path currentsVectorFilePath) {
        this.currentsVectorFilePath = currentsVectorFilePath;
    }

    @SuppressWarnings("unused")
    public Path getCurrentsVectorFilePath() {
        return currentsVectorFilePath;
    }

    @SuppressWarnings("unused")
    public void setCurrentsVectorFilePath(Path currentsVectorFilePath) {
        this.currentsVectorFilePath = currentsVectorFilePath;
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

    @Override public DriftingObjectMover apply(FishState fishState) {
        return apply(fishState.getMap());
    }
    public DriftingObjectMover apply(NauticalMap nauticalMap) {
        final Continuous2D vectorField =
            new Continuous2D(1.0, nauticalMap.getWidth(), nauticalMap.getHeight());
        final int secondsPerDay = 60 * 60 * 24;
        final List<CurrentVector> currentVectors =
            parseAll(CurrentVector.class, currentsVectorFilePath);
        currentVectors.forEach(cv -> {
            final Coordinate coord = new Coordinate(cv.lon, cv.lat);
            final Double2D xy = coordinatesToXY(nauticalMap, coord);
            final Double2D uv = metresVectorToLonLatVector(coord, cv.u, cv.v);
            final Double2D dailyUV = uv.multiply(secondsPerDay);
            vectorField.setObjectLocation(dailyUV, xy);
        });

        final Double2D nullVector = new Double2D(0, 0);
        Map<SeaTile, Double2D> currentVectorMap =
            nauticalMap.getAllSeaTilesExcludingLandAsList().stream().map(seaTile -> {
                final Coordinate coordinates = nauticalMap.getCoordinates(seaTile);
                final Double2D xy = coordinatesToXY(nauticalMap, coordinates);
                final Bag vectors = vectorField.getNeighborsExactlyWithinDistance(xy, 1.0);
                final int n = vectors.size();
                if (n == 0)
                    return new Pair<>(seaTile, nullVector);
                else {
                    final Double2D sum = MasonUtils.<Double2D>bagToStream(vectors)
                        .reduce(nullVector, Double2D::add);
                    final Double2D uv = new Double2D(sum.x / n, sum.y / n);
                    return new Pair<>(seaTile, uv);
                }
            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        return new DriftingObjectMover(nauticalMap, currentVectorMap);
    }

    public static class CurrentVector {
        @Parsed double lon;
        @Parsed double lat;
        @Parsed double u;
        @Parsed double v;
        @SuppressWarnings("unused") public void setLon(double lon) { this.lon = lon; }
        @SuppressWarnings("unused") public void setLat(double lat) { this.lat = lat; }
        @SuppressWarnings("unused") public void setU(double u) { this.u = u; }
        @SuppressWarnings("unused") public void setV(double v) { this.v = v; }
    }
}
