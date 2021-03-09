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

package uk.ac.ox.oxfish.model.data.webviz.heatmaps;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ColourMapEntry;

import java.util.Collection;
import java.util.function.DoubleSupplier;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.data.webviz.colours.ColourUtils.colourStringToHtmlCode;

@SuppressWarnings({"unused"})
public final class LogTransparencyColourMapBuilderFactory implements JsonDefinitionBuilderFactory<Collection<ColourMapEntry>> {

    private static final int NUM_GRADATIONS = 25;

    static final ImmutableMap<Double, Double> gradations =
        range(1, NUM_GRADATIONS)
            .mapToObj(x -> x * (1.0 / NUM_GRADATIONS))
            .collect(toImmutableMap(identity(), x -> 1 + Math.log10(x + (1.0 / NUM_GRADATIONS))));

    private final double MAX_OPACITY = 0.85;
    private String colour = "yellow";
    private DoubleSupplier maxValueFunction = () -> 1;

    public LogTransparencyColourMapBuilderFactory() {}

    public LogTransparencyColourMapBuilderFactory(final String colour, final DoubleSupplier maxValueFunction) {
        this.colour = colour;
        this.maxValueFunction = maxValueFunction;
    }

    public DoubleSupplier getMaxValueFunction() { return maxValueFunction; }

    public void setMaxValueFunction(final DoubleSupplier maxValueFunction) {
        this.maxValueFunction = maxValueFunction;
    }

    public String getColour() { return colour; }

    public void setColour(final String colour) { this.colour = colour; }

    /**
     * Colour maps do not have their own file names
     */
    @Override public String getBaseName() { throw new UnsupportedOperationException(); }

    @Override public JsonBuilder<Collection<ColourMapEntry>> makeDefinitionBuilder(final String scenarioTitle) {
        return fishState -> {
            final String htmlColour = colourStringToHtmlCode(this.colour);
            final double max = maxValueFunction.getAsDouble();
            final double step = 1.0 / NUM_GRADATIONS;
            return Stream.concat(
                Stream.of(new ColourMapEntry(
                    0,
                    htmlColour,
                    0,
                    true
                )),
                gradations.entrySet().stream().map(entry ->
                    new ColourMapEntry(
                        max * entry.getKey(),
                        htmlColour,
                        MAX_OPACITY * entry.getValue(),
                        true
                    )
                )
            ).collect(toImmutableList());
        };
    }

}
