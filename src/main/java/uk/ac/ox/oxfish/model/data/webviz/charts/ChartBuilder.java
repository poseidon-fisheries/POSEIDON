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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public final class ChartBuilder implements JsonBuilder<Chart>, Startable {

    private final boolean xAxisIsSimulationTimeInYears;
    private final Collection<Double> yLines;
    private final Collection<String> columns; // TODO: allow renaming columns
    private int numYearsToSkip; // will be recorded at start time

    ChartBuilder(
        final Collection<String> columns,
        final Collection<Double> yLines,
        final boolean xAxisIsSimulationTimeInYears
    ) {
        this.columns = columns;
        this.yLines = yLines;
        this.xAxisIsSimulationTimeInYears = xAxisIsSimulationTimeInYears;
    }

    @Override public Chart buildJsonObject(final FishState fishState) {
        final Collection<Integer> xData =
            range(numYearsToSkip, fishState.getYear())
                .boxed()
                .collect(toList());

        final Collection<Series> series =
            columns.stream()
                .map(fishState.getYearlyDataSet()::getColumn)
                .map(col -> col.stream().skip(numYearsToSkip).collect(toList()))
                .map(Series::new)
                .collect(toList());

        return new Chart(xData, xAxisIsSimulationTimeInYears, series, yLines);
    }

    @Override public void start(final FishState fishState) {
        // this gets started before the first day of the year, so we need to add one
        numYearsToSkip = fishState.getYear() + 1;
    }

}
