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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.RegionTypeDefinition;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.RegionsDefinition;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.data.webviz.colours.ColourSeries.SET1;

public class SpecificRegionsBuilderFactory implements RegionsBuilderFactory {

    private List<String> regionColours = SET1.getHtmlColours();

    @SuppressWarnings("unused") public List<String> getRegionColours() { return regionColours; }

    @SuppressWarnings("unused") public void setRegionColours(final List<String> regionColours) {
        this.regionColours = regionColours;
    }

    @Override public JsonBuilder<Regions> makeDataBuilder(final FishState fishState) {
        return new SpecificRegionsBuilder(this::extractSpecificRegionRegulationFactories);
    }

    @Override public JsonBuilder<RegionsDefinition> makeDefinitionBuilder(final String scenarioTitle) {
        return fishState -> new RegionsDefinition(
            makeFileName(scenarioTitle),
            range(0, extractSpecificRegionRegulationFactories(fishState).size())
                .mapToObj(i -> new RegionTypeDefinition(i, regionColours.get(i)))
                .collect(toImmutableList())
        );
    }

    @Override public String getBaseName() {
        return "Regions";
    }

    private List<AlgorithmFactory<?>> extractSpecificRegionRegulationFactories(FishState fishState) {
        return fishState.getFisherFactories()
            .stream()
            .map(Map.Entry::getValue)
            .map(FisherFactory::getRegulations)
            .flatMap(this::extractSpecificRegionRegulationFactories)
            .collect(toImmutableList());
    }

    private Stream<AlgorithmFactory<?>> extractSpecificRegionRegulationFactories(
        AlgorithmFactory<?> regulationFactory
    ) {
        if (regulationFactory instanceof MultipleRegulationsFactory)
            return ((MultipleRegulationsFactory) regulationFactory)
                .getFactories()
                .stream()
                .flatMap(this::extractSpecificRegionRegulationFactories);
        else if (regulationFactory instanceof SpecificProtectedAreaFactory)
            return Stream.of(regulationFactory);
        else if (regulationFactory instanceof TemporaryRegulationFactory) {
            final AlgorithmFactory<? extends Regulation> delegate =
                ((TemporaryRegulationFactory) regulationFactory).getDelegate();
            if (delegate instanceof SpecificProtectedAreaFactory)
                return Stream.of(regulationFactory);
            else
                return Stream.empty();
        } else
            return Stream.empty();
    }

}
