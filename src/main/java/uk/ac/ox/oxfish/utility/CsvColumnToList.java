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

package uk.ac.ox.oxfish.utility;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * Utility to read a csv file into
 * Created by carrknight on 11/29/16.
 */
public class CsvColumnToList {

    private String pathToCSV;

    private boolean headerInFile;

    private char separator;

    private int columnNumber;

    private final Function<Double,Double> transformer;


    public CsvColumnToList(String pathToCSV, boolean headerInFile, char separator,  int columnNumber) {
        this.pathToCSV = pathToCSV;
        this.headerInFile = headerInFile;
        this.separator = separator;
        this.columnNumber = columnNumber;
        this.transformer = aDouble -> aDouble;

    }

    public CsvColumnToList(
            String pathToCSV, boolean headerInFile, char separator, int columnNumber,
            Function<Double, Double> transformer) {
        this.pathToCSV = pathToCSV;
        this.headerInFile = headerInFile;
        this.separator = separator;
        this.columnNumber = columnNumber;
        this.transformer = transformer;
    }

    public LinkedList<Double> readColumn()
    {


        //turn the csv column into a list of doubles
        try {
            CSVReader  reader = new CSVReader(new FileReader(pathToCSV), separator);


            Iterator<String[]> iterator = reader.iterator();

            if(headerInFile)
                iterator.next();

            LinkedList<Double> column = new LinkedList<Double>();
            while(iterator.hasNext())
                column.add(transformer.apply(Double.parseDouble(iterator.next()[columnNumber])));

            return column;
        } catch (IOException e) {
            throw new RuntimeException("failed to read or parse " + pathToCSV  + " with exception " + e);
        }
    }

    /**
     * Getter for property 'pathToCSV'.
     *
     * @return Value for property 'pathToCSV'.
     */
    public String getPathToCSV() {
        return pathToCSV;
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
     * Getter for property 'separator'.
     *
     * @return Value for property 'separator'.
     */
    public char getSeparator() {
        return separator;
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
     * Setter for property 'pathToCSV'.
     *
     * @param pathToCSV Value to set for property 'pathToCSV'.
     */
    public void setPathToCSV(String pathToCSV) {
        this.pathToCSV = pathToCSV;
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
     * Setter for property 'separator'.
     *
     * @param separator Value to set for property 'separator'.
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * Setter for property 'columnNumber'.
     *
     * @param columnNumber Value to set for property 'columnNumber'.
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
