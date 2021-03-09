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
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections15.iterators.LoopingIterator;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonBuilder;
import uk.ac.ox.oxfish.model.data.webviz.JsonDataBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.colours.ColourSeries;
import uk.ac.ox.oxfish.model.data.webviz.colours.ColourUtils;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.ChartDefinition;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.SeriesDefinition;
import uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier;

import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static com.google.common.collect.Streams.zip;
import static uk.ac.ox.oxfish.model.data.webviz.colours.ColourSeries.SET1;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.round;

@SuppressWarnings("WeakerAccess")
public final class ChartBuilderFactory implements
    JsonDataBuilderFactory<Chart>,
    JsonDefinitionBuilderFactory<ChartDefinition> {

    public static final DoubleUnaryOperator PERCENTILE_TRANSFORMER = value -> round(value * 100d);
    public static final DoubleUnaryOperator KG_TO_T_TRANSFORMER = value -> value / 1000;

    private String title = "Chart title";
    private String xLabel = "Year";
    private String yLabel = "Value";
    private Collection<String> seriesColours = SET1.getHtmlColours();
    private boolean xAxisIsSimulationTimeInYears = true;
    private Collection<Double> yLines = ImmutableList.of();
    private Map<String, String> dataColumnNamesAndLegends = ImmutableMap.of();
    private DoubleUnaryOperator valueTransformer = v -> v;

    @NotNull public static <T> ChartBuilderFactory fromVesselClassifier(
        final String title,
        final String yLabel,
        final VesselClassifier<T> vesselClassifier,
        final Function<T, String> columnNameExtractor
    ) {

        final ChartBuilderFactory chartBuilderFactory = ChartBuilderFactory.fromSeriesIdentifiers(
            title,
            yLabel,
            vesselClassifier.getTypes(),
            columnNameExtractor,
            vesselClassifier::getLegend
        );
        chartBuilderFactory.setSeriesColours(
            vesselClassifier.getTypes().stream()
                .map(vesselClassifier::getJavaColor)
                .map(ColourUtils::javaColorToHtmlCode)
                ::iterator
        );

        return chartBuilderFactory;
    }

    @NotNull public static <T> ChartBuilderFactory fromSeriesIdentifiers(
        final String title,
        final String yLabel,
        final Iterable<T> seriesIdentifiers,
        final Function<T, String> seriesIdentifierToDataColumnName,
        final Function<T, String> seriesIdentifierToLegend
    ) {
        return fromDataColumnNames(
            title,
            yLabel,
            stream(seriesIdentifiers).collect(toImmutableMap(
                seriesIdentifierToDataColumnName,
                seriesIdentifierToLegend
            ))
        );
    }

    @NotNull public static ChartBuilderFactory fromDataColumnNames(
        final String title,
        final String yLabel,
        Map<String, String> dataColumnNamesAndLegends
    ) {
        final ChartBuilderFactory chartBuilderFactory = new ChartBuilderFactory();
        chartBuilderFactory.setTitle(title);
        chartBuilderFactory.setYLabel(yLabel);
        chartBuilderFactory.setDataColumnNamesAndLegends(ImmutableMap.copyOf(dataColumnNamesAndLegends));
        return chartBuilderFactory;
    }

    @NotNull public static ChartBuilderFactory fromColumnName(
        final String title,
        final String yLabel,
        final String columnName
    ) {
        return fromColumnNamePattern(title, yLabel, ImmutableList.of(columnName), "%s");
    }

    @NotNull public static ChartBuilderFactory fromColumnNamePattern(
        final String title,
        final String yLabel,
        final Iterable<String> baseNames,
        final String columnNameFormatPattern
    ) {
        return fromColumnNamePattern(
            title,
            yLabel,
            baseNames,
            columnNameFormatPattern,
            "%s"
        );
    }

    @NotNull public static ChartBuilderFactory fromColumnNamePattern(
        final String title,
        final String yLabel,
        final Iterable<String> baseNames,
        final String columnNameFormatPattern,
        final String columnLegendFormatPattern
    ) {
        return fromSeriesIdentifiers(
            title,
            yLabel,
            baseNames,
            v -> String.format(columnNameFormatPattern, v),
            v -> String.format(columnLegendFormatPattern, v)
        );
    }

    @SuppressWarnings("unused") public DoubleUnaryOperator getValueTransformer() { return valueTransformer; }

    public ChartBuilderFactory setValueTransformer(DoubleUnaryOperator valueTransformer) {
        this.valueTransformer = valueTransformer;
        return this;
    }

    @SuppressWarnings("unused")
    public Collection<String> getSeriesColours() { return seriesColours; }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public ChartBuilderFactory setSeriesColours(final ColourSeries seriesColours) {
        this.seriesColours = ImmutableList.copyOf(seriesColours.getHtmlColours());
        return this;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public ChartBuilderFactory setSeriesColours(final Iterable<String> seriesColours) {
        this.seriesColours = ImmutableList.copyOf(seriesColours);
        return this;
    }

    @SuppressWarnings("unused") public boolean isXAxisIsSimulationTimeInYears() { return xAxisIsSimulationTimeInYears; }

    @SuppressWarnings("unused")
    public ChartBuilderFactory setXAxisIsSimulationTimeInYears(final boolean xAxisIsSimulationTimeInYears) {
        this.xAxisIsSimulationTimeInYears = xAxisIsSimulationTimeInYears;
        return this;
    }

    @SuppressWarnings("unused") public Collection<Double> getYLines() { return yLines; }

    public ChartBuilderFactory setYLines(final Collection<Double> yLines) {
        this.yLines = yLines;
        return this;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getDataColumnNamesAndLegends() { return dataColumnNamesAndLegends; }

    @SuppressWarnings("UnusedReturnValue")
    public ChartBuilderFactory setDataColumnNamesAndLegends(final Map<String, String> dataColumnNamesAndLegends) {
        this.dataColumnNamesAndLegends = dataColumnNamesAndLegends;
        return this;
    }

    @Override public JsonBuilder<Chart> makeDataBuilder(FishState ignored) {
        return new ChartBuilder(
            dataColumnNamesAndLegends.keySet(),
            yLines,
            xAxisIsSimulationTimeInYears,
            valueTransformer
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override public JsonBuilder<ChartDefinition> makeDefinitionBuilder(final String scenarioTitle) {
        return fishState -> new ChartDefinition(
            makeFileName(scenarioTitle),
            getTitle(),
            getXLabel(),
            getYLabel(),
            zip(
                dataColumnNamesAndLegends.values().stream(),
                stream(new LoopingIterator<>(seriesColours)).map(ColourUtils::colourStringToHtmlCode),
                SeriesDefinition::new
            ).collect(toImmutableList())
        );
    }

    public String getTitle() { return title; }

    public ChartBuilderFactory setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getXLabel() { return xLabel; }

    @SuppressWarnings("unused") public ChartBuilderFactory setXLabel(final String xLabel) {
        this.xLabel = xLabel;
        return this;
    }

    public String getYLabel() { return yLabel; }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public ChartBuilderFactory setYLabel(final String yLabel) {
        this.yLabel = yLabel;
        return this;
    }

    @Override public String getBaseName() { return "Chart of " + getTitle(); }

}
