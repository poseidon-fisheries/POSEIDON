package uk.ac.ox.oxfish.utility;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.awt.*;
import java.util.Map;

public class BoatColors {
    public final static Map<String, Color> BOAT_COLORS =
        ImmutableMap.<String, Color>builder()
            .put("black", Color.black)
            .put("red", Color.red)
            .put("blue", Color.blue)
            .put("yellow", Color.yellow)
            .put("green", Color.green)
            .put("grey", Color.gray)
            .put("gray", Color.gray)
            .put("pink", Color.pink)
            .put("salmon", new Color(250, 128, 114))
            .put("palevioletred", new Color(219, 112, 147))
            .put("teal", new Color(0, 128, 128))
            .put("wheat", new Color(245, 222, 179))
            .build();

    public static boolean hasColorTag(final Fisher fisher) {
        return fisher.getTagsList().stream().anyMatch(BOAT_COLORS::containsKey);
    }
}
