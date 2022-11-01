package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import junit.framework.TestCase;

import static uk.ac.ox.oxfish.model.scenario.EpoScenario.DEFAULT_MAP_EXTENT;

public class TwoByTwoRegionalDivisionTest extends TestCase {

    public void testLocationsInDivision() {
        final TwoByTwoRegionalDivision division =
            new TwoByTwoRegionalDivision(new Coordinate(-140.5, 0.5), DEFAULT_MAP_EXTENT);

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
            assertEquals(coordinate.toString(), regionName, division.getRegion(coordinate).getName())
        ));

    }
}