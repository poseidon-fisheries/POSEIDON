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

package uk.ac.ox.oxfish.model.data.webviz.regions;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.RegionTypeDefinition;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.RegionsDefinition;

import static uk.ac.ox.oxfish.model.data.webviz.colours.ColourUtils.colourStringToHtmlCode;

public final class SingleFixedRegionBuilderFactory implements RegionsBuilderFactory {

    private int typeId;
    private String colour = "gray";

    public String getColour() { return colour; }

    public void setColour(final String colour) { this.colour = colour; }

    @SuppressWarnings("unused") public int getTypeId() { return typeId; }

    @SuppressWarnings("unused") public void setTypeId(final int typeId) { this.typeId = typeId; }

    @Override public String getBaseName() { return Regions.class.getSimpleName(); }

    @Override public JsonBuilder<Regions> makeDataBuilder(FishState ignored) {
        return new SingleFixedRegionBuilder(typeId);
    }

    @Override public JsonBuilder<RegionsDefinition> makeDefinitionBuilder(final String scenarioTitle) {
        return fishState -> new RegionsDefinition(
            makeFileName(scenarioTitle),
            ImmutableList.of(new RegionTypeDefinition(typeId, colourStringToHtmlCode(colour)))
        );
    }

}
