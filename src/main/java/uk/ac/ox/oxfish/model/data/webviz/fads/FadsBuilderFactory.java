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

package uk.ac.ox.oxfish.model.data.webviz.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDataBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.FadsDefinition;

import static uk.ac.ox.oxfish.model.data.webviz.colours.ColourUtils.colourStringToHtmlCode;

public final class FadsBuilderFactory implements
    JsonDataBuilderFactory<Fads>,
    JsonDefinitionBuilderFactory<FadsDefinition> {

    private String fadsColour = "yellow";

    @SuppressWarnings("unused")
    public String getFadsColour() { return fadsColour; }

    @SuppressWarnings("unused")
    public void setFadsColour(final String fadsColour) { this.fadsColour = fadsColour; }

    @Override public String getBaseName() { return Fads.class.getSimpleName(); }

    @Override public FadsBuilder makeDataBuilder(FishState ignored) { return new FadsBuilder(); }

    @Override public JsonBuilder<FadsDefinition> makeDefinitionBuilder(String scenarioTitle) {
        return fishState -> new FadsDefinition(makeFileName(scenarioTitle), colourStringToHtmlCode(fadsColour));
    }

}
