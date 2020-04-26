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

    public void setValueTransformer(DoubleUnaryOperator valueTransformer) {
        this.valueTransformer = valueTransformer;
    }

    @SuppressWarnings("unused")
    public Collection<String> getSeriesColours() { return seriesColours; }

    @SuppressWarnings("unused")
    public void setSeriesColours(final Iterable<String> seriesColours) {
        this.seriesColours = ImmutableList.copyOf(seriesColours);
    }

    @SuppressWarnings("unused") public boolean isXAxisIsSimulationTimeInYears() { return xAxisIsSimulationTimeInYears; }

    @SuppressWarnings("unused")
    public void setXAxisIsSimulationTimeInYears(final boolean xAxisIsSimulationTimeInYears) {
        this.xAxisIsSimulationTimeInYears = xAxisIsSimulationTimeInYears;
    }

    @SuppressWarnings("unused") public Collection<Double> getYLines() { return yLines; }

    @SuppressWarnings("unused") public void setYLines(final Collection<Double> yLines) { this.yLines = yLines; }

    @SuppressWarnings("unused")
    public Map<String, String> getDataColumnNamesAndLegends() { return dataColumnNamesAndLegends; }

    public void setDataColumnNamesAndLegends(final Map<String, String> dataColumnNamesAndLegends) {
        this.dataColumnNamesAndLegends = dataColumnNamesAndLegends;
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

    public void setTitle(final String title) { this.title = title; }

    public String getXLabel() { return xLabel; }

    @SuppressWarnings("unused") public void setXLabel(final String xLabel) { this.xLabel = xLabel; }

    public String getYLabel() { return yLabel; }

    @SuppressWarnings("unused") public void setYLabel(final String yLabel) { this.yLabel = yLabel; }

    @Override public String getBaseName() { return "Chart of " + getTitle(); }

}
