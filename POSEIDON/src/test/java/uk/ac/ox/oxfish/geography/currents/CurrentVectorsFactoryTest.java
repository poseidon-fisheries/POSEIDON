package uk.ac.ox.oxfish.geography.currents;

import com.vividsolutions.jts.geom.Coordinate;
import junit.framework.TestCase;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoGravityAbundanceScenario;

import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.SECONDS_PER_DAY;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.metrePerSecondToXyPerDaysVector;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class CurrentVectorsFactoryTest extends TestCase {

    double EARTH_CIRCUMFERENCE = 40075.017;

    public void testMetrePerSecondToXyPerDaysVector() {

        final FishState fishState = startTestableScenario(EpoGravityAbundanceScenario.class);
        final MapExtent mapExtent = fishState.getMap().getMapExtent();

        final double oneMeterPerSecondAtEquatorInDegreesPerDay =
            metrePerSecondToXyPerDaysVector(new Double2D(1, 0), new Coordinate(0, 0), mapExtent).length();

        assertEquals(
            SECONDS_PER_DAY / ((EARTH_CIRCUMFERENCE / 360) * 1000),
            oneMeterPerSecondAtEquatorInDegreesPerDay,
            EPSILON
        );
    }
}