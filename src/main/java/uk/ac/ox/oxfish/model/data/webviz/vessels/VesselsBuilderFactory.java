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
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.VesselTypeDefinition;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.VesselsDefinition;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.awt.Color.BLACK;
import static java.util.Comparator.comparingInt;

public final class VesselsBuilderFactory
    implements JsonBuilderFactory<Vessels>, JsonBuilder<VesselsDefinition> {

    private VesselClassifier vesselClassifier = new SingleTypeVesselClassifier(1, "Vessel", BLACK);

    @SuppressWarnings("unused") public VesselClassifier getVesselClassifier() { return vesselClassifier; }

    @SuppressWarnings("unused") public void setVesselClassifier(final VesselClassifier vesselClassifier) {
        this.vesselClassifier = vesselClassifier;
    }

    @Override public String getBaseName() {
        return Vessels.class.getSimpleName();
    }

    @Override public VesselsBuilder apply(final FishState fishState) {
        return new VesselsBuilder(vesselClassifier);
    }

    @Override public VesselsDefinition buildJsonObject(final FishState fishState) {
        return new VesselsDefinition(
            getFileName(),
            fishState.getFishers().stream()
                .mapToInt(vesselClassifier)
                .distinct()
                .mapToObj(typeId -> new VesselTypeDefinition(
                    typeId,
                    vesselClassifier.getLegend(typeId),
                    makeHtmlColorCode(vesselClassifier.getColour(typeId))
                ))
                .collect(toImmutableSortedSet(comparingInt(VesselTypeDefinition::getTypeId)))
        );

    }

}
