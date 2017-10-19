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

package uk.ac.ox.oxfish.geography.sampling;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 2/25/16.
 */
public class GeographicalSampleTest {

    @Test
    public void sampledMap() throws Exception {

        Path resourcesDirectory = new File("src/test/resources").toPath();
        GeographicalSample sample = new GeographicalSample(resourcesDirectory.resolve("sampled_map_test.txt"), true);
        assertEquals(sample.getMinEasting(),298500,.1);
        assertEquals(sample.getMaxEasting(),355500,.1);
        assertEquals(sample.getMinNorthing(),5345500 ,.1);
        assertEquals(sample.getMaxNorthing(),5351500,.1);

        //it should read in the way it is inserted
        assertEquals(sample.getEastings().get(0),353500,.001);
        assertEquals(sample.getNorthings().get(0),5351500,.001);
        assertEquals(sample.getObservations().get(0),0.0130458027124405,.001);
    }
}