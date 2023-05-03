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

package uk.ac.ox.oxfish.biology.initializer;

import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 6/14/17.
 */
public class DerisoParametersTest {


    public static void main(String[] args) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        DerisoParameters derisoParameters = yaml.loadAs(
                new FileReader(
                        Paths.get("inputs", "california",
                                  "biology", "Yelloweye Rockfish", "deriso.yaml")
                                .toFile()
                ),
                DerisoParameters.class
        );

        assertEquals(derisoParameters.getHistoricalYearlySurvival().get(0),
                     0.938356073678119,
                     .0001);

    }

}