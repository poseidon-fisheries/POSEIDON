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
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilder;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.events.EventBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.fads.FadsBuilder;
import uk.ac.ox.oxfish.model.data.webviz.fads.FadsBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilder;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.regions.RegionsBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.regions.SingleFixedRegionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.Scenario;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ScenarioBuilder;
import uk.ac.ox.oxfish.model.data.webviz.vessels.SingleTypeVesselClassifier;
import uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier;
import uk.ac.ox.oxfish.model.data.webviz.vessels.VesselsBuilderFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.awt.Color.BLACK;

@SuppressWarnings("unused")
public final class JsonOutputManagerFactory implements AlgorithmFactory<JsonOutputManager> {

    private String scenarioTitle = "Scenario title";
    private String scenarioDescription = "Scenario description";
    private String startDate = LocalDate.now().toString();
    private int numYearsToSkip = 0;
    private boolean prettyPrinting = false;

    private FadsBuilderFactory fadsBuilderFactory = new FadsBuilderFactory();
    private RegionsBuilderFactory regionsBuilderFactory = new SingleFixedRegionBuilderFactory();
    private VesselsBuilderFactory vesselsBuilderFactory = new VesselsBuilderFactory();
    private Collection<? extends EventBuilderFactory> eventBuilderFactories = ImmutableList.of();
    private Collection<? extends ChartBuilderFactory> chartBuilderFactories = ImmutableList.of();
    private Collection<? extends HeatmapBuilderFactory> heatmapBuilderFactories = ImmutableList.of();
    private VesselClassifier vesselClassifier = new SingleTypeVesselClassifier(1, "Vessel", BLACK);

    public Collection<? extends EventBuilderFactory> getEventBuilderFactories() { return eventBuilderFactories; }

    public void setEventBuilderFactories(Collection<? extends EventBuilderFactory> eventBuilderFactories) {
        this.eventBuilderFactories = eventBuilderFactories;
    }

    @Override public JsonOutputManager apply(final FishState fishState) {

        final FadsBuilder fadsBuilder = new FadsBuilder();

        final Instant startTime = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();

        final ImmutableList<HeatmapBuilder> heatmapBuilders =
            heatmapBuilderFactories.stream().map(factory -> factory.apply(fishState)).collect(toImmutableList());

        final ImmutableList<ChartBuilder> chartBuilders =
            chartBuilderFactories.stream().map(factory -> factory.apply(fishState)).collect(toImmutableList());

        final JsonBuilderFactory<Scenario> scenarioBuilderFactory = new JsonBuilderFactory<Scenario>() {

            @Override public String getBaseName() { return Scenario.class.getSimpleName(); }

            @Override public JsonBuilder<Scenario> apply(final FishState fishState) {
                return new ScenarioBuilder(
                    scenarioTitle,
                    scenarioDescription,
                    startTime,
                    fadsBuilderFactory,
                    regionsBuilderFactory,
                    vesselsBuilderFactory,
                    eventBuilderFactories,
                    heatmapBuilderFactories,
                    chartBuilderFactories
                );
            }
        };

        final Gson gson = prettyPrinting
            ? new GsonBuilder().setPrettyPrinting().create()
            : new GsonBuilder().create();

        return new JsonOutputManager(
            numYearsToSkip,
            new ImmutableList.Builder<JsonOutputPlugin<?>>()
                .add(vesselsBuilderFactory.makeJsonOutputPlugin(gson, fishState))
                .add(fadsBuilderFactory.makeJsonOutputPlugin(gson, fishState))
                .add(regionsBuilderFactory.makeJsonOutputPlugin(gson, fishState))
                .add(scenarioBuilderFactory.makeJsonOutputPlugin(gson, fishState))
                .addAll(heatmapBuilderFactories.stream().map(bf -> bf.makeJsonOutputPlugin(gson, fishState)).iterator())
                .addAll(chartBuilderFactories.stream().map(bf -> bf.makeJsonOutputPlugin(gson, fishState)).iterator())
                .build()
        );
    }

    public String getScenarioTitle() { return scenarioTitle; }

    public void setScenarioTitle(final String scenarioTitle) { this.scenarioTitle = scenarioTitle; }

    public String getScenarioDescription() { return scenarioDescription; }

    public void setScenarioDescription(final String scenarioDescription) {
        this.scenarioDescription = scenarioDescription;
    }

    public String getStartDate() { return startDate; }

    public void setStartDate(final String startDate) { this.startDate = startDate; }

    public int getNumYearsToSkip() { return numYearsToSkip; }

    public void setNumYearsToSkip(final int numYearsToSkip) { this.numYearsToSkip = numYearsToSkip; }

    public boolean isPrettyPrinting() { return prettyPrinting; }

    public void setPrettyPrinting(final boolean prettyPrinting) { this.prettyPrinting = prettyPrinting; }

    public FadsBuilderFactory getFadsBuilderFactory() { return fadsBuilderFactory; }

    public void setFadsBuilderFactory(final FadsBuilderFactory fadsBuilderFactory) {
        this.fadsBuilderFactory = fadsBuilderFactory;
    }

    public RegionsBuilderFactory getRegionsBuilderFactory() { return regionsBuilderFactory; }

    public void setRegionsBuilderFactory(final RegionsBuilderFactory regionsBuilderFactory) {
        this.regionsBuilderFactory = regionsBuilderFactory;
    }

    public VesselsBuilderFactory getVesselsBuilderFactory() { return vesselsBuilderFactory; }

    public void setVesselsBuilderFactory(final VesselsBuilderFactory vesselsBuilderFactory) {
        this.vesselsBuilderFactory = vesselsBuilderFactory;
    }

    public Collection<? extends ChartBuilderFactory> getChartBuilderFactories() { return chartBuilderFactories; }

    public void setChartBuilderFactories(final Collection<? extends ChartBuilderFactory> chartBuilderFactories) {
        this.chartBuilderFactories = chartBuilderFactories;
    }

    public Collection<? extends HeatmapBuilderFactory> getHeatmapBuilderFactories() { return heatmapBuilderFactories; }

    public void setHeatmapBuilderFactories(final Collection<? extends HeatmapBuilderFactory> heatmapBuilderFactories) {
        this.heatmapBuilderFactories = heatmapBuilderFactories;
    }

    public VesselClassifier getVesselClassifier() { return vesselClassifier; }

    public void setVesselClassifier(final VesselClassifier vesselClassifier) {
        this.vesselClassifier = vesselClassifier;
    }

    private String makeOutputFileName(final Class<?> clazz) { return clazz.getSimpleName() + ".json"; }

}
