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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Created by carrknight on 11/30/16.
 */
public class CsvColumnsToListsTest {


    @Test
    public void csvColumn() throws Exception {

        final CsvColumnsToLists tester = new CsvColumnsToLists(
            Paths.get("inputs", "tests", "weather.csv").toAbsolutePath().toString(),
            ',', "alsowrong,wrong".split(",")
        );

        final LinkedList<Double>[] columns = tester.readColumns();
        Assertions.assertEquals(columns[0].get(0), -1, .0001);
        Assertions.assertEquals(columns[1].get(0), 0, .0001);

    }
}