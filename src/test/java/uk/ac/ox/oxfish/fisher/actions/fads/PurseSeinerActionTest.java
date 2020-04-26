package uk.ac.ox.oxfish.fisher.actions.fads;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.TicTacToeRegionalDivision;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple50x50Map;

public class PurseSeinerActionTest {

    @Test
    public void getRegionNumber() {
        final NauticalMap map = generateSimple50x50Map().getMap();
        final RegionalDivision regionalDivision = new TicTacToeRegionalDivision(map);

        ImmutableMap.<SeaTile, String>builder()
            .put(map.getSeaTile(0, 0), "Northwest")
            .put(map.getSeaTile(16, 0), "Northwest")
            .put(map.getSeaTile(17, 0), "North")
            .put(map.getSeaTile(map.getWidth() - 1, 0), "Northeast")
            .put(map.getSeaTile(16, 16), "Northwest")
            .put(map.getSeaTile(17, 16), "North")
            .put(map.getSeaTile(map.getWidth() - 1, 16), "Northeast")
            .put(map.getSeaTile(16, 17), "West")
            .put(map.getSeaTile(17, 17), "Central")
            .put(map.getSeaTile(map.getWidth() - 1, 17), "East")
            .put(map.getSeaTile(16, map.getHeight() - 1), "Southwest")
            .put(map.getSeaTile(17, map.getHeight() - 1), "South")
            .put(map.getSeaTile(map.getWidth() - 1, map.getHeight() - 1), "Southeast")
            .build()
            .forEach((seaTile, region) -> assertEquals(region, regionalDivision.getRegion(seaTile).getName()));
    }

}