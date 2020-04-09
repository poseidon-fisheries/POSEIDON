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

import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ColourMapEntry;

import java.util.Collection;
import java.util.function.DoubleSupplier;

public class MonochromeGradientColourMapBuilderFactory implements
    JsonDefinitionBuilderFactory<Collection<ColourMapEntry>> {

    private final DoubleSupplier maxValueFunction;
    private String colour;

    MonochromeGradientColourMapBuilderFactory(
        final String colour,
        final DoubleSupplier maxValueFunction
    ) {
        this.colour = colour;
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
            final GradientColourMapBuilderFactory gradientColourMapBuilderFactory =
                new GradientColourMapBuilderFactory();
            gradientColourMapBuilderFactory.setMinColour(colour);
            gradientColourMapBuilderFactory.setMaxColour(colour);
            gradientColourMapBuilderFactory.setMaxValueFunction(maxValueFunction);
            return gradientColourMapBuilderFactory
                .makeDefinitionBuilder(scenarioTitle)
                .buildJsonObject(fishState);
        };
    }

}
