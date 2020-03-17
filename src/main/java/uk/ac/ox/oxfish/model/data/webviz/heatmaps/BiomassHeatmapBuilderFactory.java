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
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ColourMapEntry;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.HeatmapDefinition;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

import static java.util.Objects.requireNonNull;

public final class BiomassHeatmapBuilderFactory implements HeatmapBuilderFactory {

    private String speciesName = "Species 0";
    private GradientColourMapBuilderFactory colourMapBuilderFactory =
        new GradientColourMapBuilderFactory();

    public static BiomassHeatmapBuilderFactory newInstance(final String speciesName) {
        final BiomassHeatmapBuilderFactory instance = new BiomassHeatmapBuilderFactory();
        instance.speciesName = speciesName;
        return instance;
    }

    @Override public String getTitle() { return speciesName + " biomass"; }

    @Override public JsonBuilderFactory<Collection<ColourMapEntry>> getColourMapBuilderFactory() {
        return colourMapBuilderFactory;
    }

    public void setColourMapBuilderFactory(final GradientColourMapBuilderFactory colourMapBuilderFactory) {
        this.colourMapBuilderFactory = colourMapBuilderFactory;
    }

    public String getSpeciesName() { return speciesName; }

    public void setSpeciesName(final String speciesName) { this.speciesName = speciesName; }

    @Override public HeatmapDefinition buildJsonObject(final FishState fishState) {
        final Species species = getSpecies(fishState);
        fishState.getMap().getAllSeaTilesExcludingLandAsList().stream()
            .map(SeaTile::getBiology)
            .filter(biology -> biology instanceof VariableBiomassBasedBiology)
            .map(biology -> (VariableBiomassBasedBiology) biology)
            .mapToDouble(biology -> biology.getCarryingCapacity(species))
            .max()
            .ifPresent(colourMapBuilderFactory::setMaxValue);
        return HeatmapBuilderFactory.super.buildJsonObject(fishState);
    }

    @Override public ToDoubleFunction<SeaTile> makeNumericExtractor(final FishState fishState) {
        final Species specie = getSpecies(fishState);
        return seaTile -> seaTile.getBiomass(specie);
    }

    @NotNull private Species getSpecies(final FishState fishState) {
        final Species species = fishState.getBiology().getSpecie(speciesName);
        requireNonNull(species, speciesName + " not defined in global biology.");
        return species;
    }

}
