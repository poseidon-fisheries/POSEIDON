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

abstract public class AbstractIntervalHeatmapBuilderFactory
    implements HeatmapBuilderFactory {

    private int interval;
    private String colour;

    private TimestepsBuilder timestepsBuilder = null; // will be set when the builder is constructed

    AbstractIntervalHeatmapBuilderFactory() { this(30, "green"); }

    AbstractIntervalHeatmapBuilderFactory(final int interval, final String colour) {
        this.interval = interval;
        this.colour = colour;
    }

    public int getInterval() { return interval; }

    public void setInterval(final int interval) { this.interval = interval; }

    public String getColour() { return colour; }

    public void setColour(final String colour) { this.colour = colour; }

    TimestepsBuilder getTimestepsBuilder() { return timestepsBuilder; }

    void setTimestepsBuilder(final TimestepsBuilder timestepsBuilder) {
        this.timestepsBuilder = timestepsBuilder;
    }

}
