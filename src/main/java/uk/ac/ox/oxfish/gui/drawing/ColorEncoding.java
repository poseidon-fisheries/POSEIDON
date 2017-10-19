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
