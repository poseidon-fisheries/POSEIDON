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

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * utility to read multiple columnNames of a csv file and turn them into multiple lists of doubles
 * This utility differs from CSVColumnToList for being heading based.
 *
 * It assumes " is used as a quoting character
 * Created by carrknight on 11/30/16.
 */
public class CsvColumnsToLists
{


    /**
     * path to file
     */
    private String pathToCSV;

    /**
     * csv separator
     */
    private char separator;

    /**
     * columnNames to read
     */
    private String[] columnNames;

    public CsvColumnsToLists(String pathToCSV, char separator, String[] columnNames) {
        this.pathToCSV = pathToCSV;
        this.separator = separator;
        this.columnNames = columnNames;
        Preconditions.checkArgument(columnNames.length>=1, "no columnNames given to read");
        //trim them all
        for(int i=0; i<columnNames.length; i++)
            columnNames[i] = columnNames[i].replace("\"", "").trim().toLowerCase();
    }

    public LinkedList<Double>[] readColumns()
    {


        //turn the csv column into a list of doubles
        try {
            FileReader io = new FileReader(pathToCSV);
            CSVReader reader = new CSVReader(io, separator);
            io.close();


            Iterator<String[]> iterator = reader.iterator();

            String[] heading = iterator.next();
            LinkedList<Integer> indices = new LinkedList<Integer>();

            //find the column index of each column name
            headloop:
            for(int i=0; i<columnNames.length; i++)
            {
                for(int j=0; j<heading.length; j++) {
                    if (heading[j].replace("\"", "").trim().toLowerCase().equals(columnNames[i]))
                    {
                        indices.add(j);
                        continue headloop;
                    }
                }
                throw new IllegalArgumentException("Failed to find column " + columnNames[i] +
                                                           " in the heading: " + Arrays.toString(heading));

            }

            //create an array of columns to return
            LinkedList<Double>[] column = new LinkedList[columnNames.length];
            for(int i=0; i<column.length; i++)
                column[i] = new LinkedList<>();

            assert column.length == indices.size();
            while(iterator.hasNext())
            {
                String[] line = iterator.next();
                for(int i=0; i<column.length; i++)
                    column[i].add(Double.parseDouble(line[indices.get(i)]));
            }
            reader.close();
            return column;
        } catch (IOException e) {
            throw new RuntimeException("failed to read or parse " + pathToCSV  + " with exception " + e);
        }
    }


}
