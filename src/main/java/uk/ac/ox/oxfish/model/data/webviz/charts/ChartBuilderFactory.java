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

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections15.iterators.LoopingIterator;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ChartDefinition;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.SeriesDefinition;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static com.google.common.collect.Streams.zip;

@SuppressWarnings("WeakerAccess")
public final class ChartBuilderFactory implements
    JsonBuilderFactory<Chart>,
    JsonBuilder<ChartDefinition> {

    private String title = "Chart title";
    private String xLabel = "Year";
    private String yLabel = "Value";
    private Collection<String> seriesColours = ImmutableList.of(
        // https://colorbrewer2.org/#type=qualitative&scheme=Set1&n=9
        "#e41a1c", "#377eb8", "#4daf4a", "#984ea3", "#ff7f00", "#ffff33", "#a65628", "#f781bf", "#999999"
    );
    private boolean xAxisIsSimulationTimeInYears = true;
    private Collection<Double> yLines = ImmutableList.of();
    private Collection<String> columns = ImmutableList.of(); // TODO: allow renaming columns

    /**
     * @param title             The title of the chart to be produced by the factory.
     * @param columnNamePattern The pattern to be passed to {@code String.format} to generate column names using {@code values}.
     * @param values            The used to generate the column names.
     * @return A new {@code ChartBuilderFactory}.
     */
    @NotNull public static ChartBuilderFactory forPattern(
        final String title,
        final String columnNamePattern,
        final Iterable<String> values
    ) {
        return forColumns(title, stream(values).map(s -> String.format(columnNamePattern, s))::iterator);
    }

    @NotNull public static ChartBuilderFactory forColumns(final String title, final Iterable<String> columnNames) {
        final ChartBuilderFactory chartBuilderFactory = new ChartBuilderFactory();
        chartBuilderFactory.setTitle(title);
        chartBuilderFactory.setColumns(ImmutableList.copyOf(columnNames));
        return chartBuilderFactory;
    }

    @SuppressWarnings("unused")
    public Collection<String> getSeriesColours() { return seriesColours; }

    @SuppressWarnings("unused")
    public void setSeriesColours(final Collection<String> seriesColours) { this.seriesColours = seriesColours; }

    public String getTitle() { return title; }

    public void setTitle(final String title) { this.title = title; }

    public String getXLabel() { return xLabel; }

    @SuppressWarnings("unused") public void setXLabel(final String xLabel) { this.xLabel = xLabel; }

    public String getYLabel() { return yLabel; }

    @SuppressWarnings("unused") public void setYLabel(final String yLabel) { this.yLabel = yLabel; }

    @SuppressWarnings("unused") public boolean isXAxisIsSimulationTimeInYears() { return xAxisIsSimulationTimeInYears; }

    @SuppressWarnings("unused")
    public void setXAxisIsSimulationTimeInYears(final boolean xAxisIsSimulationTimeInYears) {
        this.xAxisIsSimulationTimeInYears = xAxisIsSimulationTimeInYears;
    }

    @SuppressWarnings("unused") public Collection<Double> getYLines() { return yLines; }

    @SuppressWarnings("unused") public void setYLines(final Collection<Double> yLines) { this.yLines = yLines; }

    public Collection<String> getColumns() { return columns; }

    public void setColumns(final Collection<String> columns) {
        this.columns = columns;
    }

    @Override public ChartBuilder apply(final FishState fishState) {
        return new ChartBuilder(columns, yLines, xAxisIsSimulationTimeInYears);
    }

    @Override public String getBaseName() { return title + " Chart"; }

    @SuppressWarnings("UnstableApiUsage")
    @Override public ChartDefinition buildJsonObject(final FishState fishState) {
        return new ChartDefinition(
            getFileName(),
            getTitle(),
            getXLabel(),
            getYLabel(),
            zip(
                columns.stream(),
                stream(new LoopingIterator<>(seriesColours)).map(this::colourStringToHtmlCode),
                SeriesDefinition::new
            ).collect(toImmutableList())
        );
    }

}
