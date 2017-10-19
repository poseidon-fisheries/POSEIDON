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

import java.awt.Color;

import com.google.common.base.Preconditions;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;

/**
 * This is actually modified from the original TriColorMap made by the very good Joey Harrison
 * who originally coded it for the RiftLand project. Thank you Joey. All I added was a second constructor.
 *
 * TriColorMap is a gradient between three colors. It works the same as
 * SimpleColorMap, and indeed, makes use of that class.
 * For example:
 * -1 : red
 *  0 : white
 *  1 : blue
 *
 *  Values between -1 and 0 get mapped to some shade of pink, while values
 *  between 0 and 1 would get mapped to some shade of blue
 *
 * @author Joey Harrison & carrknight
 *
 */
public class TriColorMap implements ColorMap
{
    private SimpleColorMap lowerHalf;
    private SimpleColorMap upperHalf;
    private final double mid;
    private final Color minColor;
    private final Color midColorLow;
    private final Color midColorHigh;
    private final Color maxColor;



    public TriColorMap(double min, double mid, double max, Color minColor, Color midColor, Color maxColor) {
       this(min,mid,max,minColor,midColor,midColor,maxColor);

    }

    public TriColorMap(double min, double mid, double max, Color minColor, Color  midColorLow,
                       Color midColorHigh, Color maxColor) {
        this.mid = mid;
        lowerHalf = new SimpleColorMap(min, mid, minColor, midColorLow);
        upperHalf = new SimpleColorMap(mid, max, midColorHigh, maxColor);
        this.minColor=minColor;
        this.midColorLow=midColorLow;
        this.midColorHigh=midColorHigh;
        this.maxColor=maxColor;
    }

    @Override
    public Color getColor(double level) {

        if(Double.isNaN(level))
            return Color.GRAY;

        if (level < mid)
            return lowerHalf.getColor(level);
        else
            return upperHalf.getColor(level);
    }

    @Override
    public int getRGB(double level) {

        if(Double.isNaN(level))
            return Color.GRAY.getRGB();

        if (level < mid)
            return lowerHalf.getRGB(level);
        else
            return upperHalf.getRGB(level);
    }

    @Override
    public int getAlpha(double level) {

        if(Double.isNaN(level))
            return Color.GRAY.getAlpha();


        if (level < mid)
            return lowerHalf.getAlpha(level);
        else
            return upperHalf.getAlpha(level);
    }

    @Override
    public boolean validLevel(double level) {
        if (level < mid)
            return lowerHalf.validLevel(level);
        else
            return upperHalf.validLevel(level);
    }

    @Override
    public double defaultValue() {
        return mid;
    }


    public void resetMax(double max)
    {
        Preconditions.checkArgument(max>mid);
        upperHalf = new SimpleColorMap(mid, max, midColorHigh, maxColor);
    }


    public void resetMin(double min)
    {
        Preconditions.checkArgument(min<mid);
        upperHalf = new SimpleColorMap(min, mid, minColor, midColorLow);
    }
}
