package uk.ac.ox.oxfish.geography.currents;

import com.vividsolutions.jts.geom.Coordinate;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO.ZERO_VECTOR;
import static uk.ac.ox.oxfish.utility.CsvLogger.addCsvLogger;

public class CurrentVectorsEPOTest extends TestCase {
    @SuppressWarnings("CommentedOutCode")
    public void testEpoScenarioHasNoDeadCells() {
        final FishState fishState = new FishState();
        fishState.setScenario(new EpoAbundanceScenario());
        fishState.start();
        final CurrentVectors currentVectors = fishState.getFadMap().getDriftingObjectsMap().getCurrentVectors();
        final NauticalMap nauticalMap = fishState.getMap();
        final Coordinate lagoDeMaracaibo = new Coordinate(-71.5, 9.5);
        final Coordinate lakeSuperior = new Coordinate(-86.5, 47.5);
        final List<ObjectArrayMessage> currentlessCells = nauticalMap
            .getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(seaTile -> {
                // it's fine if we don't have currents for these two lakes
                final Coordinate coordinates = nauticalMap.getCoordinates(seaTile);
                return !coordinates.equals(lagoDeMaracaibo) && !coordinates.equals(lakeSuperior);
            })
            .flatMap(seaTile ->
                range(0, 365 * 2)
                    .filter(step -> currentVectors.getVector(step, seaTile.getGridLocation()).equals(ZERO_VECTOR))
                    .mapToObj(step -> {
                        final Coordinate coordinates = nauticalMap.getCoordinates(seaTile);
                        return new ObjectArrayMessage(coordinates.x, coordinates.y, step);
                    })
            ).collect(toImmutableList());
        // Uncomment the next two lines to log currentless cells to a CSV file:
        // final Logger logger = addCsvLogger(Level.DEBUG, "currentless_cells", "lon,lat,step");
        // currentlessCells.forEach(logger::debug);
        assertTrue(currentlessCells.isEmpty());
    }
}