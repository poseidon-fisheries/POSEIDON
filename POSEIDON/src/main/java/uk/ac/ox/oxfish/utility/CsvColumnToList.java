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

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility to read a csv file into Created by carrknight on 11/29/16.
 */
public class CsvColumnToList {

    private final Function<Double, Double> transformer;
    private String pathToCSV;
    private boolean headerInFile;
    private char separator;
    private int columnNumber;

    public CsvColumnToList(
        final String pathToCSV,
        final boolean headerInFile,
        final char separator,
        final int columnNumber
    ) {
        this.pathToCSV = pathToCSV;
        this.headerInFile = headerInFile;
        this.separator = separator;
        this.columnNumber = columnNumber;
        this.transformer = aDouble -> aDouble;

    }

    public CsvColumnToList(
        final String pathToCSV,
        final boolean headerInFile,
        final char separator,
        final int columnNumber,
        final Function<Double, Double> transformer
    ) {
        this.pathToCSV = pathToCSV;
        this.headerInFile = headerInFile;
        this.separator = separator;
        this.columnNumber = columnNumber;
        this.transformer = transformer;
    }

    public LinkedList<Double> readColumn() {
        try (final Reader reader = new FileReader(pathToCSV)) {
            final CsvParserSettings settings = new CsvParserSettings();
            settings.setHeaderExtractionEnabled(headerInFile);
            settings.getFormat().setDelimiter(separator);
            return new CsvParser(settings)
                .parseAll(reader)
                .stream()
                .map(strings -> transformer.apply(Double.parseDouble(strings[columnNumber])))
                .collect(Collectors.toCollection(LinkedList::new));
        } catch (final IOException e) {
            throw new RuntimeException(MessageFormat.format(
                "failed to read or parse {0} with exception {1}",
                pathToCSV,
                e
            ));
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
     * Setter for property 'pathToCSV'.
     *
     * @param pathToCSV Value to set for property 'pathToCSV'.
     */
    public void setPathToCSV(final String pathToCSV) {
        this.pathToCSV = pathToCSV;
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
    public void setHeaderInFile(final boolean headerInFile) {
        this.headerInFile = headerInFile;
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
    public void setSeparator(final char separator) {
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
    public void setColumnNumber(final int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
