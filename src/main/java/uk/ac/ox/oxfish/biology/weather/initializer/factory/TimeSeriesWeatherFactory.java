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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.biology.weather.initializer.CSVWeatherInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnToList;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Constant temperature and direction but wind speed follows a time series
 * Created by carrknight on 11/29/16.
 */
public class TimeSeriesWeatherFactory implements AlgorithmFactory<CSVWeatherInitializer>
{


    /**
     * locker holding the weather object
     */
    private Locker<String,CSVWeatherInitializer> weatherLocker = new Locker<>();


    private CsvColumnToList reader = new CsvColumnToList(Paths.get("inputs","test","weather.csv").toAbsolutePath().toString(),
                                                         true,
                                                         ',',
                                                         1) ;



    private DoubleParameter temperature = new FixedDoubleParameter(30);


    public TimeSeriesWeatherFactory() {
    }


    public TimeSeriesWeatherFactory(String pathToTimeSeries, boolean headerInFile, char separator, int columnNumber) {
        reader= new CsvColumnToList(pathToTimeSeries,headerInFile,separator,columnNumber);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CSVWeatherInitializer apply(FishState state)
    {

        return weatherLocker.presentKey(state.getHopefullyUniqueID(),

                                 new Supplier<CSVWeatherInitializer>() {
                                     @Override
                                     public CSVWeatherInitializer get() {

                                        return new CSVWeatherInitializer(
                                                reader
                                        );
                                 }
        });


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
     * Getter for property 'headerInFile'.
     *
     * @return Value for property 'headerInFile'.
     */
    public boolean isHeaderInFile() {
        return reader.isHeaderInFile();
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
     * Getter for property 'columnNumber'.
     *
     * @return Value for property 'columnNumber'.
     */
    public int getColumnNumber() {
        return reader.getColumnNumber();
    }

    /**
     * Setter for property 'pathToCSV'.
     *
     * @param pathToCSV Value to set for property 'pathToCSV'.
     */
    public void setPathToCSV(String pathToCSV) {
        reader.setPathToCSV(pathToCSV);
    }

    /**
     * Setter for property 'headerInFile'.
     *
     * @param headerInFile Value to set for property 'headerInFile'.
     */
    public void setHeaderInFile(boolean headerInFile) {
        reader.setHeaderInFile(headerInFile);
    }

    /**
     * Setter for property 'separator'.
     *
     * @param separator Value to set for property 'separator'.
     */
    public void setSeparator(char separator) {
        reader.setSeparator(separator);
    }

    /**
     * Setter for property 'columnNumber'.
     *
     * @param columnNumber Value to set for property 'columnNumber'.
     */
    public void setColumnNumber(int columnNumber) {
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

    public void setTemperature(DoubleParameter temperature) {
        this.temperature = temperature;
    }
}
