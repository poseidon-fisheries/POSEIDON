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

import com.google.common.collect.ImmutableList;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class ColourSeries {

    // https://colorbrewer2.org/#type=qualitative&scheme=Set1&n=9
    public static final ColourSeries SET1 = new ColourSeries(
        "#e41a1c", "#377eb8", "#4daf4a", "#984ea3", "#ff7f00", "#ffff33", "#a65628", "#f781bf", "#999999"
    );
    private final List<String> htmlColors;
    private final List<Color> javaColors;

    @SuppressWarnings("unused")
    public ColourSeries(Color... javaColors) {
        this.javaColors = ImmutableList.copyOf(javaColors);
        this.htmlColors = Arrays.stream(javaColors)
            .map(ColourUtils::javaColorToHtmlCode)
            .collect(toImmutableList());
    }

    public ColourSeries(String... htmlColors) {
        this.htmlColors = ImmutableList.copyOf(htmlColors);
        this.javaColors = Arrays.stream(htmlColors)
            .map(ColourUtils::colourStringToJavaColor)
            .collect(toImmutableList());
    }

    public List<String> getHtmlColours() { return htmlColors; }

    public List<Color> getJavaColors() { return javaColors; }

}
