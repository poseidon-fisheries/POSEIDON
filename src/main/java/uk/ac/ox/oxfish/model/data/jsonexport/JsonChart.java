package uk.ac.ox.oxfish.model.data.jsonexport;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JsonChart {

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
        this(title, xLabel, yLabel, xData, dataColumns.stream().map(JsonSeries::new).collect(toList()), yLines);
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
