/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by carrknight on 8/17/16.
 */
public class LowessTileTest {


    @Test
    public void regression() throws Exception {

        List<String> data = Files.readAllLines(Paths.get("inputs", "tests", "regression.csv"));
        Assertions.assertEquals(data.size(), 100);

        LeastSquareFilter tile = new LeastSquareFilter(2, 1000d, new double[]{0, 0}, 1d);

        for (String line : data) {
            String[] split = line.split(",");
            Assertions.assertEquals(split.length, 2);
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            tile.addObservation(new double[]{1, x}, y, 9);
        }
        System.out.println(Arrays.toString(tile.getBeta()));
        Assertions.assertEquals(1.97711, tile.getBeta()[0], .01);
        Assertions.assertEquals(4.85097, tile.getBeta()[1], .01);
    }
}
