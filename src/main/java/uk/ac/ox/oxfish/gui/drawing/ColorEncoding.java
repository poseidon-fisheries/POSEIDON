package uk.ac.ox.oxfish.gui.drawing;

import sim.util.gui.ColorMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.function.Function;

/**
 * A color map and a way to encode the seatile in a way that is understandable
 * Created by carrknight on 9/28/15.
 */
public class ColorEncoding {



    private final ColorMap map;

    private final Function<SeaTile,Double> encoding;

    /**
     * whether we expect this to change values over time
     */
    private final boolean immutable;

    public ColorEncoding(
            ColorMap map, Function<SeaTile, Double> encoding, boolean immutable) {
        this.map = map;
        this.encoding = encoding;
        this.immutable = immutable;
    }

    public ColorMap getMap() {
        return map;
    }

    public Function<SeaTile, Double> getEncoding() {
        return encoding;
    }

    public boolean isImmutable() {
        return immutable;
    }
}
