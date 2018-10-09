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

import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.function.Function;

/**
 * Created by carrknight on 7/12/17.
 */
public class SelfAdjustingColorEncoding extends ColorEncoding {

    private double maxValue;

    private double minValue;

    private final Function<SeaTile,Double> adjustingEncoding;


    public SelfAdjustingColorEncoding(
            SimpleColorMap map,
            Function<SeaTile, Double> encoding, boolean immutable, double maxValue, double minValue) {
        super(map, encoding, immutable);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.adjustingEncoding = encoding.andThen(
                new Function<Double, Double>() {
                    @Override
                    public Double apply(Double value) {
                        if(!Double.isFinite(value))
                            return Double.NaN;

                        if(value>SelfAdjustingColorEncoding.this.maxValue)
                        {
                            SelfAdjustingColorEncoding.this.maxValue = value;
                            map.setLevels(
                                    SelfAdjustingColorEncoding.this.minValue,
                                    SelfAdjustingColorEncoding.this.maxValue,
                                    map.getColor(SelfAdjustingColorEncoding.this.minValue ),
                                    map.getColor(SelfAdjustingColorEncoding.this.maxValue )
                            );
                        }
                        return value;

                    }
                }
        );
    }

    @Override
    public Function<SeaTile, Double> getEncoding() {
        return adjustingEncoding;
    }
}
