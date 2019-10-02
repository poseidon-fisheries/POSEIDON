package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JsonChart {

    private static List<String> colours = ImmutableList.of(
        String.format("#%02x%02x%02x", 0, 0, 0),
        String.format("#%02x%02x%02x", 230, 159, 0),
        String.format("#%02x%02x%02x", 86, 180, 233),
        String.format("#%02x%02x%02x", 0, 158, 150),
        String.format("#%02x%02x%02x", 240, 228, 66)
    );
    String title;
    String xLabel;
    String yLabel;
    List<Double> xData;
    List<JsonSeries> series;
    List<Double> yLines;

    public JsonChart(String title, String xLabel, String yLabel, List<Double> xData, List<JsonSeries> series, List<Double> yLines) {
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.xData = xData;
        this.series = series;
        this.yLines = yLines;
    }

    public JsonChart(List<DataColumn> dataColumns, String title, String xLabel, String yLabel, List<Double> xData, List<Double> yLines) {
        this(
            title, xLabel, yLabel, xData,
            Streams.zip(
                dataColumns.stream(),
                colours.stream(),
                (dataColumn, colour) -> new JsonSeries(dataColumn, dataColumn.getName(), colour)
            ).collect(toList()),
            yLines
        );
    }

    public JsonChart(FishState model, List<String> columnNames, String title, String xLabel, String yLabel, List<Double> xData, List<Double> yLines) {
        this(
            columnNames.stream().map(model.getYearlyDataSet()::getColumn).collect(toList()),
            title, xLabel, yLabel, xData, yLines
        );
    }

    public List<JsonSeries> getSeries() {
        return series;
    }

}
