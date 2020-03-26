package uk.ac.ox.oxfish.fisher.actions.fads;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.Regions;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple50x50Map;

public class PurseSeinerActionTest {

    @Test
    public void getRegionNumber() {
        final NauticalMap map = generateSimple50x50Map().getMap();

        ImmutableMap.<SeaTile, Integer>builder()
            .put(map.getSeaTile(0, 0), 11)
            .put(map.getSeaTile(16, 0), 11)
            .put(map.getSeaTile(17, 0), 21)
            .put(map.getSeaTile(map.getWidth() - 1, 0), 31)
            .put(map.getSeaTile(16, 16), 11)
            .put(map.getSeaTile(17, 16), 21)
            .put(map.getSeaTile(map.getWidth() - 1, 16), 31)
            .put(map.getSeaTile(16, 17), 12)
            .put(map.getSeaTile(17, 17), 22)
            .put(map.getSeaTile(map.getWidth() - 1, 17), 32)
            .put(map.getSeaTile(16, map.getHeight() - 1), 13)
            .put(map.getSeaTile(17, map.getHeight() - 1), 23)
            .put(map.getSeaTile(map.getWidth() - 1, map.getHeight() - 1), 33)
            .build()
            .forEach((seaTile, region) -> assertEquals((int) region, Regions.getRegionNumber(map, seaTile)));
    }
}