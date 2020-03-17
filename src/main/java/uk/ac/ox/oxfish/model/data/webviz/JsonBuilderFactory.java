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

package uk.ac.ox.oxfish.model.data.webviz;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.awt.*;
import java.lang.reflect.Field;

public interface JsonBuilderFactory<T> extends AlgorithmFactory<JsonBuilder<T>> {

    @SuppressWarnings("UnstableApiUsage") default Escaper getFileNameEscaper() {
        return new PercentEscaper("(),-_ ", false);
    }

    String getBaseName();

    default String getFileName() { return getFileNameEscaper().escape(getBaseName()) + ".json"; }

    default String colourStringToHtmlCode(final String colour) { return makeHtmlColorCode(readColour(colour)); }

    default String makeHtmlColorCode(@NotNull final Color color) {
        // https://stackoverflow.com/a/15114020/487946
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
    }

    default Color readColour(final String colour) {
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

    default JsonOutputPlugin<T> makeJsonOutputPlugin(final Gson gson, final FishState fishState) {
        return new JsonOutputPlugin<>(gson, apply(fishState), getFileName());
    }

}