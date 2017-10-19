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

package uk.ac.ox.oxfish.geography.ports;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 3/13/17.
 */
public class PortListFactoryTest {


    @Test
    public void yamlOut() throws Exception {

        PortListFactory factory = new PortListFactory();

        factory.getPorts().put("Washington",new Coordinate(0,0));
        factory.getPorts().put("Italy",new Coordinate(5,2));

        FishYAML yaml = new FishYAML();
        String dump = yaml.dump(factory);
        System.out.println(dump);

        assertEquals(dump.trim(),
                     "List of Ports:\n" +
                             "  ports:\n" +
                             "    Washington: 'x:0.0,y:0.0'\n" +
                             "    Italy: 'x:5.0,y:2.0'"
                     );

    }


    @Test
    public void yamlIn() throws Exception {


        FishYAML yaml = new FishYAML();
        System.out.println(Pattern.
                matches("[\\s'\"]*x:[0-9]+\\.?[0-9]*,y:[0-9]+\\.?[0-9]*[\\s'\"]*", "x:7.0,y:2.0" ));
        System.out.println(Pattern.
                matches("[\\s'\"]*x:[0-9]+\\.?[0-9]*,y:[0-9]+\\.?[0-9]*[\\s'\"]*", "  'x:7.0,y:2.0'" ));

        PortListFactory factory = yaml.loadAs("List of Ports:\n" +
                                       "  ports:\n" +
                                       "    Washington: x:0.0,y:0.0\n" +
                                       "    Italy: x:7.0,y:2.0",
                               PortListFactory.class);


        assertEquals(factory.getPorts().size(),2);
        assertEquals((int)factory.getPorts().get("Washington").x,0);
        assertEquals((int)factory.getPorts().get("Italy").x,7);

    }
}