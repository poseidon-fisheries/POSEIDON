package uk.ac.ox.oxfish.geography.currents;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoGravityAbundanceScenario;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO.ZERO_VECTOR;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class CurrentVectorsEPOTest {
    @Test
    public void testEpoScenarioHasNoDeadCells() {
        final FishState fishState = new FishState();
        fishState.setScenario(new EpoGravityAbundanceScenario());
        fishState.start();
        final CurrentVectors currentVectors = fishState.getFadMap().getDriftingObjectsMap().getCurrentVectors();
        final NauticalMap nauticalMap = fishState.getMap();
        final Coordinate lagoDeMaracaibo = new Coordinate(-71.5, 9.5);
        final Coordinate lakeSuperior = new Coordinate(-86.5, 47.5);
        final List<Map.Entry<Coordinate, Integer>> currentlessCells = nauticalMap
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
                    .mapToObj(step -> entry(nauticalMap.getCoordinates(seaTile), step))
            ).collect(toImmutableList());
        Assertions.assertTrue(currentlessCells.isEmpty());
    }
}