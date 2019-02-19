package uk.ac.ox.oxfish.geography.fads;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

    private Path currentsVectorFile;

    public DriftingObjectMoverFactory(Path currentsVectorFile) {
        this.currentsVectorFile = currentsVectorFile;
    }

    @SuppressWarnings("unused")
    public Path getCurrentsVectorFile() {
        return currentsVectorFile;
    }

    @SuppressWarnings("unused")
    public void setCurrentsVectorFile(Path currentsVectorFile) {
        this.currentsVectorFile = currentsVectorFile;
    }

    /**
     * Transforms a lon/lat coordinate to an x/y coordinate that can be used with a continuous field
     * covering the same space as the nautical map. This is basically a floating point version of
     * GeomGridField.toXCoord/.toYCoord; not sure why it doesn't exist in GeomVectorField in the
     * first place. We might consider moving this method to NauticalMap if it turns out to be
     * useful.
     */
    private Double2D coordinatesToXY(NauticalMap nauticalMap, Coordinate coord) {
        final Envelope mbr = nauticalMap.getRasterBathymetry().getMBR();
        final double w = nauticalMap.getRasterBathymetry().getPixelWidth();
        final double h = nauticalMap.getRasterBathymetry().getPixelHeight();
        final double x = (coord.x - mbr.getMinX()) / w;
        final double y = (mbr.getMaxY() - coord.y) / h;
        return new Double2D(x, y);
    }

    /**
     * Reads the UV current vectors from a CSV file assumed to have lon, lat, u and v columns.
     */
    private List<Pair<Coordinate, Double2D>> readCurrentVectors() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(settings);
        String fileName = currentsVectorFile.normalize().toString();
        try {
            final Reader reader = new FileReader(fileName);
            return parser.parseAllRecords(reader).stream().map(record -> {
                final Integer lon = record.getInt("lon");
                final Integer lat = record.getInt("lat");
                final Double u = record.getDouble("u");
                final Double v = record.getDouble("v");
                return new Pair<>(new Coordinate(lon, lat), new Double2D(u, v));
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to read or parse " + fileName + " with exception: " + e
            );
        }
    }

    /**
     * Takes a vector of offsets in metres and converts it to a vector of longitude/latitude
     * offsets, assuming that we are in the vicinity of {@code coord}. Adapted from
     * https://stackoverflow.com/a/2839560 and https://stackoverflow.com/a/7478827.
     */
    private Double2D metresVectorToLonLatVector(Coordinate coord, Double2D uv) {
        double r = EquirectangularDistance.EARTH_RADIUS * 1000; // Earth radius in metres
        final double dx = (180 / Math.PI) * (uv.x / r) / Math.cos(Math.PI / 180.0 * coord.y);
        final double dy = (180 / Math.PI) * (uv.y / r);
        return new Double2D(dx, dy);
    }

    @Override public DriftingObjectMover apply(FishState fishState) {
        final NauticalMap nauticalMap = fishState.getMap();
        final Continuous2D vectorField =
            new Continuous2D(1.0, nauticalMap.getWidth(), nauticalMap.getHeight());
        final int secondsPerDay = 60 * 60 * 24;
        readCurrentVectors().forEach(pair -> {
            final Coordinate coord = pair.getFirst();
            final Double2D xy = coordinatesToXY(nauticalMap, coord);
            final Double2D uv = metresVectorToLonLatVector(coord, pair.getSecond());
            final Double2D dailyUV = uv.multiply(secondsPerDay);
            vectorField.setObjectLocation(dailyUV, xy);
        });

        final Double2D nullVector = new Double2D(0, 0);
        Map<SeaTile, Double2D> currentVectors =
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

        return new DriftingObjectMover(nauticalMap, currentVectors);
    }
}
