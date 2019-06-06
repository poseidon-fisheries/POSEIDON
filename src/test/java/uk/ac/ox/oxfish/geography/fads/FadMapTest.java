package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableList;
import org.apache.sis.measure.Quantities;
import org.junit.Test;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentMaps;
import uk.ac.ox.oxfish.geography.currents.VectorGrid2D;

import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.utility.Measures.TONNE;

public class FadMapTest {

    @Test
    public void fadBeaching() {

        // Make a 3x3 map, with a one tile island in the middle
        NauticalMap nauticalMap = makeMap(new int[][]{
            {-1, -1, -1},
            {-1, 10, -1},
            {-1, -1, -1}
        });

        // Make a current map that moves FADs west
        final Double2D currentVector = new Double2D(-0.01, 0);
        final Map<SeaTile, Double2D> vectors = nauticalMap
            .getAllSeaTilesExcludingLandAsList().stream()
            .collect(toMap(identity(), seaTile -> currentVector));
        final VectorGrid2D vectorGrid2D = new VectorGrid2D(nauticalMap.getWidth(), nauticalMap.getHeight(), vectors);
        final CurrentMaps currentMaps = new CurrentMaps(ImmutableList.of(vectorGrid2D), t -> 0);
        final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));
        final FadInitializer fadInitializer = new FadInitializer(Quantities.create(1, TONNE), 0);
        final FadMap fadMap = new FadMap(nauticalMap, currentMaps, globalBiology, fadInitializer);
        final FadManager fadManager = new FadManager(fadMap, 1);

        final Schedule schedule = mock(Schedule.class);
        final SimState simState = mock(SimState.class);
        simState.schedule = schedule;

        // Put a FAD at the East edge of the central row
        final Fad fad = fadMap.deployFad(fadManager, new Double2D(2, 1));

        // Let it drift to the island

        fadMap.step(simState);

        // Check that the fish are released in right cells

    }

}