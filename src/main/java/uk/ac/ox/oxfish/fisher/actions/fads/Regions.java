package uk.ac.ox.oxfish.fisher.actions.fads;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;

import static java.lang.Math.floor;

public class Regions {

    public static final Map<Integer, String> REGION_NAMES = ImmutableMap.<Integer, String>builder()
        .put(11, "Northwest")
        .put(21, "North")
        .put(31, "Northeast")
        .put(12, "West")
        .put(22, "Central")
        .put(32, "East")
        .put(13, "Southwest")
        .put(23, "South")
        .put(33, "Southeast")
        .build();

    public static int getRegionNumber(NauticalMap map, SeaTile seaTile) {
        final double divisions = 3.0;
        final double regionWidth = map.getWidth() / divisions;
        final double regionHeight = map.getHeight() / divisions;
        return (int) ((1 + floor(seaTile.getGridX() / regionWidth)) * 10) +
            (int) (1 + floor(seaTile.getGridY() / regionHeight));
    }

}
