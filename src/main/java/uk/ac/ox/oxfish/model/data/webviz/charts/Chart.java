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

package uk.ac.ox.oxfish.model.data.webviz.charts;

import java.util.Collection;

@SuppressWarnings({"FieldCanBeLocal", "unused", "WeakerAccess"})
public final class Chart {

    private final Collection<Integer> xData;
    private final boolean xAxisIsSimulationTimeInYears;
    private final Collection<Series> series;
    private final Collection<Double> yLines;

    Chart(
        final Collection<Integer> xData,
        final boolean xAxisIsSimulationTimeInYears,
        final Collection<Series> series,
        final Collection<Double> yLines
    ) {
        this.xData = xData;
        this.xAxisIsSimulationTimeInYears = xAxisIsSimulationTimeInYears;
        this.series = series;
        this.yLines = yLines;
    }

}
