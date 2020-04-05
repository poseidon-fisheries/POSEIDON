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

package uk.ac.ox.oxfish.model.data.webviz.scenarios;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilderFactory;

import java.time.Instant;
import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class ScenarioBuilder implements JsonBuilder<Scenario> {

    private final String title;
    private final String description;
    private final Instant startTime;
    private final JsonBuilder<FadsDefinition> fadsDefinitionBuilder;
    private final JsonBuilder<RegionsDefinition> regionsDefinitionBuilder;
    private final JsonBuilder<VesselsDefinition> vesselsDefinitionBuilder;
    private final Collection<? extends HeatmapBuilderFactory> heatmapDefinitionBuilders;
    private final Collection<? extends ChartBuilderFactory> chartDefinitionBuilders;

    public ScenarioBuilder(
        final String title,
        final String description,
        final Instant startTime,
        final JsonBuilder<FadsDefinition> fadsDefinitionBuilder,
        final JsonBuilder<RegionsDefinition> regionsDefinitionBuilder,
        final JsonBuilder<VesselsDefinition> vesselsDefinitionBuilder,
        final Collection<? extends HeatmapBuilderFactory> heatmapDefinitionBuilders,
        final Collection<? extends ChartBuilderFactory> chartDefinitionBuilders
    ) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.fadsDefinitionBuilder = fadsDefinitionBuilder;
        this.regionsDefinitionBuilder = regionsDefinitionBuilder;
        this.vesselsDefinitionBuilder = vesselsDefinitionBuilder;
        this.heatmapDefinitionBuilders = heatmapDefinitionBuilders;
        this.chartDefinitionBuilders = chartDefinitionBuilders;
    }

    @Override public Scenario buildJsonObject(final FishState fishState) {

        final NauticalMap map = fishState.getMap();
        final Envelope mbr = map.getRasterBathymetry().MBR;

        final Collection<PortDefinition> portDefinitions = fishState.getPorts().stream()
            .map(port -> {
                final Coordinate coordinates = map.getCoordinates(port.getLocation());
                return new PortDefinition(port.getName(), ImmutableList.of(coordinates.x, coordinates.y));
            })
            .collect(toImmutableList());

        final GridDefinition gridDefinition = new GridDefinition(
            ImmutableList.of(map.getWidth(), map.getHeight()),
            ImmutableList.of(mbr.getMinX(), mbr.getMaxY()),
            ImmutableList.of(mbr.getMaxX(), mbr.getMinY())
        );

        return new Scenario(
            title,
            description,
            startTime.getEpochSecond(),
            portDefinitions,
            vesselsDefinitionBuilder.buildJsonObject(fishState),
            fadsDefinitionBuilder.buildJsonObject(fishState),
            gridDefinition,
            regionsDefinitionBuilder.buildJsonObject(fishState),
            heatmapDefinitionBuilders.stream().map(b -> b.buildJsonObject(fishState)).collect(toImmutableList()),
            chartDefinitionBuilders.stream().map(b -> b.buildJsonObject(fishState)).collect(toImmutableList())
        );
    }

}
