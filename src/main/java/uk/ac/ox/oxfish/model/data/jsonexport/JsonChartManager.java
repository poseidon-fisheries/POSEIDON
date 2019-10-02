package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class JsonChartManager implements OutputPlugin {

    private String title;
    private String xLabel;
    private String yLabel;
    private List<Double> yLines;
    private String fileName;
    private List<String> columnNames;
    private JsonChart jsonChart;
    private Map<String, String> renamedColumns;

    public JsonChartManager(String title, String xLabel, String yLabel, List<Double> yLines, String fileName, List<String> columnNames, Map<String, String> renamedColumns) {
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.yLines = yLines;
        this.fileName = fileName;
        this.columnNames = columnNames;
        this.renamedColumns = renamedColumns;
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {
        final List<Double> xData = range(0, state.getYear()).mapToObj(i -> (double) i).collect(toList());
        jsonChart = new JsonChart(state, columnNames, title, xLabel, yLabel, xData, yLines);
        for (JsonSeries series : jsonChart.getSeries()) {
            series.setName(renamedColumns.getOrDefault(series.getName(), series.getName()));
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String composeFileContents() {
        Preconditions.checkNotNull(jsonChart);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonChart);
    }
}
