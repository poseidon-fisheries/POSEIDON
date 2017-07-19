package uk.ac.ox.oxfish.model.market.gas;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnToList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Created by carrknight on 7/18/17.
 */
public class CsvTimeSeriesGasFactory implements AlgorithmFactory<TimeSeriesGasPriceMaker>{


    private Path csvFile = Paths.get("inputs","california","2001_gasprice.csv");

    private char separator = ',';

    private int columnNumber = 1;

    /**
     * multiply this for any element in the actual csv. Usually prices are in $/gallon rather than $/liter
     */
    private double scaling = 0.219969157;

    private boolean headerInFile = true;

    private boolean loopThroughTheCSV = false;

    private boolean yearly = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TimeSeriesGasPriceMaker apply(FishState state) {

        CsvColumnToList gasPrices = new CsvColumnToList(
                csvFile.toString(),
                true,
                separator,
                columnNumber,
                new Function<Double, Double>() {
                    @Override
                    public Double apply(Double dollarsPerGallon) {
                        return  dollarsPerGallon * scaling; //ratio
                    }
                }
        );

        return new TimeSeriesGasPriceMaker(
                gasPrices.readColumn(),
                loopThroughTheCSV,
                yearly ? IntervalPolicy.EVERY_YEAR : IntervalPolicy.EVERY_DAY
        );


    }

    /**
     * Getter for property 'csvFile'.
     *
     * @return Value for property 'csvFile'.
     */
    public Path getCsvFile() {
        return csvFile;
    }

    /**
     * Setter for property 'csvFile'.
     *
     * @param csvFile Value to set for property 'csvFile'.
     */
    public void setCsvFile(Path csvFile) {
        this.csvFile = csvFile;
    }


    /**
     * Getter for property 'separator'.
     *
     * @return Value for property 'separator'.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Setter for property 'separator'.
     *
     * @param separator Value to set for property 'separator'.
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * Getter for property 'columnNumber'.
     *
     * @return Value for property 'columnNumber'.
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Setter for property 'columnNumber'.
     *
     * @param columnNumber Value to set for property 'columnNumber'.
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Getter for property 'scaling'.
     *
     * @return Value for property 'scaling'.
     */
    public double getScaling() {
        return scaling;
    }

    /**
     * Setter for property 'scaling'.
     *
     * @param scaling Value to set for property 'scaling'.
     */
    public void setScaling(double scaling) {
        this.scaling = scaling;
    }

    /**
     * Getter for property 'headerInFile'.
     *
     * @return Value for property 'headerInFile'.
     */
    public boolean isHeaderInFile() {
        return headerInFile;
    }

    /**
     * Setter for property 'headerInFile'.
     *
     * @param headerInFile Value to set for property 'headerInFile'.
     */
    public void setHeaderInFile(boolean headerInFile) {
        this.headerInFile = headerInFile;
    }

    /**
     * Getter for property 'loopThroughTheCSV'.
     *
     * @return Value for property 'loopThroughTheCSV'.
     */
    public boolean isLoopThroughTheCSV() {
        return loopThroughTheCSV;
    }

    /**
     * Setter for property 'loopThroughTheCSV'.
     *
     * @param loopThroughTheCSV Value to set for property 'loopThroughTheCSV'.
     */
    public void setLoopThroughTheCSV(boolean loopThroughTheCSV) {
        this.loopThroughTheCSV = loopThroughTheCSV;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }

    /**
     * Setter for property 'yearly'.
     *
     * @param yearly Value to set for property 'yearly'.
     */
    public void setYearly(boolean yearly) {
        this.yearly = yearly;
    }
}
