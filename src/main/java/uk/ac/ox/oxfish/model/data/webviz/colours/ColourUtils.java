/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.webviz.colours;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;

public class ColourUtils {

    private ColourUtils() {}

    public static String colourStringToHtmlCode(final String colour) {
        return javaColorToHtmlCode(colourStringToJavaColor(colour));
    }

    public static String javaColorToHtmlCode(@NotNull final Color javaColor) {
        // https://stackoverflow.com/a/15114020/487946
        return String.format("#%06x", javaColor.getRGB() & 0x00FFFFFF);
    }

    static Color colourStringToJavaColor(final String colour) {
        // adapted from https://stackoverflow.com/a/2854058/487946
        Color color;
        try {
            final Field field = Color.class.getField(colour);
            color = (Color) field.get(null);
        } catch (final Exception e) {
            color = Color.decode(colour);
        }
        return color;
    }

}
