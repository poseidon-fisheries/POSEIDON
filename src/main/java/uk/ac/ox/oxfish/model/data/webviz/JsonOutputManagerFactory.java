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

package uk.ac.ox.oxfish.model.data.webviz;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.events.EventDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.fads.FadsBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.regions.RegionsBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.regions.SingleFixedRegionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.Scenario;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ScenarioBuilder;
import uk.ac.ox.oxfish.model.data.webviz.vessels.VesselsBuilderFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;

public final class JsonOutputManagerFactory implements AlgorithmFactory<JsonOutputManager> {

    private String scenarioTitle = "Scenario title";
    private String scenarioDescription = "Scenario description";
    private String startDate = LocalDate.now().toString();
    private int numYearsToSkip = 0;
    private boolean prettyPrinting = false;
    private FadsBuilderFactory fadsBuilderFactory = new FadsBuilderFactory();
    private RegionsBuilderFactory regionsBuilderFactory = new SingleFixedRegionBuilderFactory();
    private VesselsBuilderFactory vesselsBuilderFactory = new VesselsBuilderFactory();
    private Collection<? extends EventDefinitionBuilderFactory> eventBuilderFactories = ImmutableList.of();
    private Collection<? extends ChartBuilderFactory> chartBuilderFactories = ImmutableList.of();
    private Collection<? extends HeatmapBuilderFactory> heatmapBuilderFactories = ImmutableList.of();

    @SuppressWarnings("unused")
    public Collection<? extends EventDefinitionBuilderFactory> getEventBuilderFactories() { return eventBuilderFactories; }

    public void setEventBuilderFactories(Collection<? extends EventDefinitionBuilderFactory> eventBuilderFactories) {
        this.eventBuilderFactories = eventBuilderFactories;
    }

    @Override public JsonOutputManager apply(final FishState fishState) {

        final Instant startTime = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();

        final JsonDataBuilderFactory<Scenario> scenarioBuilderFactory = new JsonDataBuilderFactory<Scenario>() {

            @Override public String getBaseName() { return Scenario.class.getSimpleName(); }

            @Override public JsonBuilder<Scenario> makeDataBuilder(FishState ignored) {
                return new ScenarioBuilder(
                    scenarioTitle,
                    scenarioDescription,
                    startTime,
                    fadsBuilderFactory.makeDefinitionBuilder(scenarioTitle),
                    regionsBuilderFactory.makeDefinitionBuilder(scenarioTitle),
                    vesselsBuilderFactory.makeDefinitionBuilder(scenarioTitle),
                    eventBuilderFactories.stream().map(bf -> bf.makeDefinitionBuilder(scenarioTitle))::iterator,
                    heatmapBuilderFactories.stream().map(bf -> bf.makeDefinitionBuilder(scenarioTitle))::iterator,
                    chartBuilderFactories.stream().map(bf -> bf.makeDefinitionBuilder(scenarioTitle))::iterator
                );
            }
        };

        final GsonBuilder gsonBuilder = new GsonBuilder();
        if (prettyPrinting) gsonBuilder.setPrettyPrinting();
        final Gson gson = gsonBuilder.create();

        return new JsonOutputManager(
            numYearsToSkip,
            new ImmutableList.Builder<JsonOutputPlugin<?>>()
                .add(scenarioBuilderFactory.makeJsonOutputPlugin(fishState, gson, scenarioTitle))
                .add(vesselsBuilderFactory.makeJsonOutputPlugin(fishState, gson, scenarioTitle))
                .add(fadsBuilderFactory.makeJsonOutputPlugin(fishState, gson, scenarioTitle))
                .add(regionsBuilderFactory.makeJsonOutputPlugin(fishState, gson, scenarioTitle))
                .addAll(heatmapBuilderFactories.stream()
                    .map(bf -> bf.makeJsonOutputPlugin(fishState, gson, scenarioTitle))
                    .iterator())
                .addAll(chartBuilderFactories.stream()
                    .map(bf -> bf.makeJsonOutputPlugin(fishState, gson, scenarioTitle))
                    .iterator())
                .build()
        );
    }

    @SuppressWarnings("unused") public String getScenarioTitle() { return scenarioTitle; }

    public void setScenarioTitle(final String scenarioTitle) { this.scenarioTitle = scenarioTitle; }

    @SuppressWarnings("unused") public String getScenarioDescription() { return scenarioDescription; }

    public void setScenarioDescription(final String scenarioDescription) {
        this.scenarioDescription = scenarioDescription;
    }

    @SuppressWarnings("unused") public String getStartDate() { return startDate; }

    public void setStartDate(final String startDate) { this.startDate = startDate; }

    @SuppressWarnings("unused") public int getNumYearsToSkip() { return numYearsToSkip; }

    public void setNumYearsToSkip(final int numYearsToSkip) { this.numYearsToSkip = numYearsToSkip; }

    @SuppressWarnings("unused") public boolean isPrettyPrinting() { return prettyPrinting; }

    public void setPrettyPrinting(final boolean prettyPrinting) { this.prettyPrinting = prettyPrinting; }

    @SuppressWarnings("unused") public FadsBuilderFactory getFadsBuilderFactory() { return fadsBuilderFactory; }

    @SuppressWarnings("unused") public void setFadsBuilderFactory(final FadsBuilderFactory fadsBuilderFactory) {
        this.fadsBuilderFactory = fadsBuilderFactory;
    }

    @SuppressWarnings("unused")
    public RegionsBuilderFactory getRegionsBuilderFactory() { return regionsBuilderFactory; }

    @SuppressWarnings("unused")
    public void setRegionsBuilderFactory(final RegionsBuilderFactory regionsBuilderFactory) {
        this.regionsBuilderFactory = regionsBuilderFactory;
    }

    public VesselsBuilderFactory getVesselsBuilderFactory() { return vesselsBuilderFactory; }

    @SuppressWarnings("unused")
    public void setVesselsBuilderFactory(final VesselsBuilderFactory vesselsBuilderFactory) {
        this.vesselsBuilderFactory = vesselsBuilderFactory;
    }

    @SuppressWarnings("unused")
    public Collection<? extends ChartBuilderFactory> getChartBuilderFactories() { return chartBuilderFactories; }

    public void setChartBuilderFactories(final Collection<? extends ChartBuilderFactory> chartBuilderFactories) {
        this.chartBuilderFactories = chartBuilderFactories;
    }

    @SuppressWarnings("unused")
    public Collection<? extends HeatmapBuilderFactory> getHeatmapBuilderFactories() { return heatmapBuilderFactories; }

    public void setHeatmapBuilderFactories(final Collection<? extends HeatmapBuilderFactory> heatmapBuilderFactories) {
        this.heatmapBuilderFactories = heatmapBuilderFactories;
    }

}
