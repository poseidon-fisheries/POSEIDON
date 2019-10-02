package uk.ac.ox.oxfish.model.data.jsonexport;

import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.LinkedList;

public class JsonSeries {
    String name;
    LinkedList<Double> yValues;
    String colour;
    public JsonSeries(DataColumn dataColumn, String name, String colour) {
        this(dataColumn.copy(), name, colour);
    }

    public JsonSeries(DataColumn dataColumn) {
        this(dataColumn.copy(), dataColumn.getName(), null);
    }

    public JsonSeries(LinkedList<Double> yValues, String name, String colour) {
        this.name = name;
        this.yValues = yValues;
        this.colour = colour;
    }

    public LinkedList<Double> getYValues() {
        return yValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
