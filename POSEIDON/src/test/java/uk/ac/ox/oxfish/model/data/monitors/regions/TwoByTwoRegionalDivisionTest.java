package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;

public class TwoByTwoRegionalDivisionTest {

    @Test
    public void testLocationsInDivision() {
        final MapExtent mapExtent = new MapExtentFactory(
            101, 100, -171, -70, -50, 50
        ).get();

        final TwoByTwoRegionalDivision division =
            new TwoByTwoRegionalDivision(new Coordinate(-140.5, 0.5), mapExtent);

        final ImmutableMap<Coordinate, String> testPoints =
            new ImmutableMap.Builder<Coordinate, String>()
                .put(new Coordinate(-149.5, 49.5), "Northwest")
                .put(new Coordinate(-140.5, 49.5), "Northwest")
                .put(new Coordinate(-149.5, 0.5), "Northwest")
                .put(new Coordinate(-140.5, 0.5), "Northwest")
                .put(new Coordinate(-149.5, -49.5), "Southwest")
                .put(new Coordinate(-140.5, -49.5), "Southwest")
                .put(new Coordinate(-149.5, -0.5), "Southwest")
                .put(new Coordinate(-140.5, -0.5), "Southwest")
                .put(new Coordinate(-139.5, 49.5), "Northeast")
                .put(new Coordinate(-70.5, 49.5), "Northeast")
                .put(new Coordinate(-139.5, 0.5), "Northeast")
                .put(new Coordinate(-70.5, 0.5), "Northeast")
                .put(new Coordinate(-139.5, -49.5), "Southeast")
                .put(new Coordinate(-70.5, -49.5), "Southeast")
                .put(new Coordinate(-139.5, -0.5), "Southeast")
                .put(new Coordinate(-70.5, -0.5), "Southeast")
                .build();

        testPoints.forEach(((coordinate, regionName) ->
            Assertions.assertEquals(regionName, division.getRegion(coordinate).getName(), coordinate.toString())
        ));

    }
}
