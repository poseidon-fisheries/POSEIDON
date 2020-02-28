/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.data.jsonexport.heatmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class JsonTimestepGridTest {


    @Test
    public void gridTest() {

        double[] row = new double[]{1,2,3};
        double[] row2 = new double[]{3,2,1};

        double[][] values = new double[2][];
        values[0] = row;
        values[1] = row2;
        JsonTimestepGrid grid = new JsonTimestepGrid(values,10);


        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(grid));

    }
}