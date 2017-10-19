/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
