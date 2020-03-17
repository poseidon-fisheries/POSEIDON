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

import java.util.Collection;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class Scenario {

    private final String title;
    private final String description;
    private final long startUnixTime;
    private final Collection<PortDefinition> ports;
    private final VesselsDefinition vessels;
    private final FadsDefinition fads;
    private final GridDefinition grid;
    private final RegionsDefinition regions;
    private final Collection<HeatmapDefinition> heatmaps;
    private final Collection<ChartDefinition> charts;

    Scenario(
        final String title,
        final String description,
        final long startUnixTime,
        final Collection<PortDefinition> portDefinitions,
        final VesselsDefinition vesselsDefinition,
        final FadsDefinition fadsDefinition,
        final GridDefinition gridDefinition,
        final RegionsDefinition regionsDefinition,
        final Collection<HeatmapDefinition> heatmapDefinitions,
        final Collection<ChartDefinition> chartDefinitions
    ) {
        this.title = title;
        this.description = description;
        this.startUnixTime = startUnixTime;
        this.ports = portDefinitions;
        this.vessels = vesselsDefinition;
        this.fads = fadsDefinition;
        this.grid = gridDefinition;
        this.regions = regionsDefinition;
        this.heatmaps = heatmapDefinitions;
        this.charts = chartDefinitions;
    }

}
