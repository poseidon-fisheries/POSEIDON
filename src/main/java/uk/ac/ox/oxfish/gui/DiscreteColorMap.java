package uk.ac.ox.oxfish.gui;

import ec.util.MersenneTwisterFast;
import sim.util.gui.ColorMap;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Created by carrknight on 11/30/16.
 */
public class DiscreteColorMap implements ColorMap {



    private final MersenneTwisterFast randomizer;

    HashMap<Integer,Color> colorsPreAssigned = new HashMap<>();


    public DiscreteColorMap(MersenneTwisterFast randomizer) {
        this.randomizer = randomizer;
    }

    @Override
    public Color getColor(double v) {
        int discretized = (int) v;
        return colorsPreAssigned.computeIfAbsent(discretized, new Function<Integer, Color>() {
            @Override
            public Color apply(Integer integer) {
                return new Color( randomizer.nextInt(256),
                                  randomizer.nextInt(256),
                                  randomizer.nextInt(256),
                                  255);
            }
        });
    }

    @Override
    public int getRGB(double v) {
        return getColor(v).getRGB();
    }

    @Override
    public int getAlpha(double v) {
        return 255;
    }

    @Override
    public boolean validLevel(double v) {
        return v>= -FishStateUtilities.EPSILON;
    }

    @Override
    public double defaultValue() {
        return -1;
    }
}
