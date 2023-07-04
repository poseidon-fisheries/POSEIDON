/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import uk.ac.ox.oxfish.biology.weather.initializer.CSVWeatherInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnToList;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;

/**
 * Constant temperature and direction but wind speed follows a time series
 * Created by carrknight on 11/29/16.
 */
public class TimeSeriesWeatherFactory implements AlgorithmFactory<CSVWeatherInitializer> {


    /**
     * locker holding the weather object
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, CSVWeatherInitializer> weatherLocker =
        new uk.ac.ox.oxfish.utility.Locker<>();

    private CsvColumnToList reader = new CsvColumnToList(
        Paths.get("inputs", "test", "weather.csv").toAbsolutePath().toString(),
        true,
        ',',
        1
    );


    private DoubleParameter temperature = new FixedDoubleParameter(30);


    public TimeSeriesWeatherFactory() {
    }


    public TimeSeriesWeatherFactory(
        final String pathToTimeSeries,
        final boolean headerInFile,
        final char separator,
        final int columnNumber
    ) {
        reader = new CsvColumnToList(pathToTimeSeries, headerInFile, separator, columnNumber);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CSVWeatherInitializer apply(final FishState state) {

        return weatherLocker.presentKey(
            state.getUniqueID(),
            () -> new CSVWeatherInitializer(reader)
        );


    }


    /**
     * Getter for property 'pathToCSV'.
     *
     * @return Value for property 'pathToCSV'.
     */
    public String getPathToCSV() {
        return reader.getPathToCSV();
    }

    /**
     * Setter for property 'pathToCSV'.
     *
     * @param pathToCSV Value to set for property 'pathToCSV'.
     */
    public void setPathToCSV(final String pathToCSV) {
        reader.setPathToCSV(pathToCSV);
    }

    /**
     * Getter for property 'headerInFile'.
     *
     * @return Value for property 'headerInFile'.
     */
    public boolean isHeaderInFile() {
        return reader.isHeaderInFile();
    }

    /**
     * Setter for property 'headerInFile'.
     *
     * @param headerInFile Value to set for property 'headerInFile'.
     */
    public void setHeaderInFile(final boolean headerInFile) {
        reader.setHeaderInFile(headerInFile);
    }

    /**
     * Getter for property 'separator'.
     *
     * @return Value for property 'separator'.
     */
    public char getSeparator() {
        return reader.getSeparator();
    }

    /**
     * Setter for property 'separator'.
     *
     * @param separator Value to set for property 'separator'.
     */
    public void setSeparator(final char separator) {
        reader.setSeparator(separator);
    }

    /**
     * Getter for property 'columnNumber'.
     *
     * @return Value for property 'columnNumber'.
     */
    public int getColumnNumber() {
        return reader.getColumnNumber();
    }

    /**
     * Setter for property 'columnNumber'.
     *
     * @param columnNumber Value to set for property 'columnNumber'.
     */
    public void setColumnNumber(final int columnNumber) {
        reader.setColumnNumber(columnNumber);
    }


    /**
     * Getter for property 'temperature'.
     *
     * @return Value for property 'temperature'.
     */
    public DoubleParameter getTemperature() {
        return temperature;
    }

    public void setTemperature(final DoubleParameter temperature) {
        this.temperature = temperature;
    }
}
