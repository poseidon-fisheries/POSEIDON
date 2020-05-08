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

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.heatmaps.BiomassHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;

import java.awt.*;
import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static uk.ac.ox.oxfish.model.data.webviz.colours.ColourUtils.javaColorToHtmlCode;

public final class BiomassSnapshotHeatmapBuilderFactory extends HeatmapBuilderFactory {

    private String speciesName = "Species 0";

    public static Collection<BiomassSnapshotHeatmapBuilderFactory> forSpecies(
        Iterable<String> speciesNames,
        Color javaColor,
        int interval
    ) {
        return stream(speciesNames)
            .map(speciesName -> {
                final BiomassSnapshotHeatmapBuilderFactory instance = new BiomassSnapshotHeatmapBuilderFactory();
                instance.speciesName = speciesName;
                instance.setColour(javaColorToHtmlCode(javaColor));
                instance.setInterval(interval);
                return instance;
            })
            .collect(toImmutableList());
    }

    public String getSpeciesName() { return speciesName; }

    public void setSpeciesName(final String speciesName) { this.speciesName = speciesName; }

    @Override HeatmapGatherer makeHeatmapGatherer(final FishState fishState) {
        return new BiomassHeatmapGatherer(getInterval(), getSpecies(fishState));
    }

    @NotNull private Species getSpecies(final FishState fishState) {
        final Species species = fishState.getBiology().getSpecie(speciesName);
        requireNonNull(species, speciesName + " not defined in global biology.");
        return species;
    }

    @Override public String getTitle() { return speciesName + " biomass"; }

    @Override public String getLegend() { return getTitle() + " (t)"; }

}
