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

package uk.ac.ox.oxfish.model.data.webviz.vessels;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDataBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.VesselTypeDefinition;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.VesselsDefinition;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.awt.Color.WHITE;
import static uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier.singleTypeClassifier;

public final class VesselsBuilderFactory implements
    JsonDataBuilderFactory<Vessels>,
    JsonDefinitionBuilderFactory<VesselsDefinition> {

    private VesselClassifier<?> vesselClassifier = singleTypeClassifier("Vessels", WHITE);

    @SuppressWarnings("unused") public VesselClassifier<?> getVesselClassifier() { return vesselClassifier; }

    @SuppressWarnings("unused") public void setVesselClassifier(final VesselClassifier<?> vesselClassifier) {
        this.vesselClassifier = vesselClassifier;
    }

    @Override public String getBaseName() {
        return Vessels.class.getSimpleName();
    }

    @Override public JsonBuilder<VesselsDefinition> makeDefinitionBuilder(final String scenarioTitle) {
        return __ -> new VesselsDefinition(
            makeFileName(scenarioTitle),
            2.0, // TODO: make this configurable
            vesselClassifier.getTypeIds().stream()
                .map(typeId -> new VesselTypeDefinition(
                    typeId,
                    vesselClassifier.getLegend(typeId),
                    vesselClassifier.getJavaColor(typeId)
                ))
                .collect(toImmutableList())
        );
    }

    @Override public JsonBuilder<Vessels> makeDataBuilder(FishState ignored) {
        return new VesselsBuilder(vesselClassifier);
    }

}
