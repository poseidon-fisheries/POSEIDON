package uk.ac.ox.oxfish.geography.currents;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.MapExtentFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.SECONDS_PER_DAY;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.metrePerSecondToXyPerDaysVector;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class CurrentVectorsFactoryTest {

    double EARTH_CIRCUMFERENCE = 40075.017;

    @Test
    public void testMetrePerSecondToXyPerDaysVector() {

        final MapExtent mapExtent = new MapExtentFactory(
            101, 100, -171, -70, -50, 50
        ).get();

        final double oneMeterPerSecondAtEquatorInDegreesPerDay =
            metrePerSecondToXyPerDaysVector(new Double2D(1, 0), new Coordinate(0, 0), mapExtent).length();

        Assertions.assertEquals(
            SECONDS_PER_DAY / ((EARTH_CIRCUMFERENCE / 360) * 1000),
            oneMeterPerSecondAtEquatorInDegreesPerDay,
            EPSILON
        );
    }
}
