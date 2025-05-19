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

package uk.ac.ox.oxfish.geography.ports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 3/13/17.
 */
public class PortListFactoryTest {


    @Test
    public void yamlOut() throws Exception {

        final PortListFactory factory = new PortListFactory();
        factory.getPorts().clear();

        factory.getPorts().put("Washington", "0.0,0.0");
        factory.getPorts().put("Italy", "5.0,2.0");

        final FishYAML yaml = new FishYAML();
        final String dump = yaml.dump(factory);
        System.out.println(dump);

        Assertions.assertEquals(dump.trim(), "List of Ports:\n" +
            "  ports:\n" +
            "    Washington: 0.0,0.0\n" +
            "    Italy: 5.0,2.0\n" +
            "  usingGridCoordinates: true");

    }


    @Test
    public void yamlIn() throws Exception {


        final FishYAML yaml = new FishYAML();
        System.out.println(Pattern.
            matches("[\\s'\"]*x:[0-9]+\\.?[0-9]*,y:[0-9]+\\.?[0-9]*[\\s'\"]*", "x:7.0,y:2.0"));
        System.out.println(Pattern.
            matches("[\\s'\"]*x:[0-9]+\\.?[0-9]*,y:[0-9]+\\.?[0-9]*[\\s'\"]*", "  'x:7.0,y:2.0'"));

        final PortListFactory factory = yaml.loadAs(
            "List of Ports:\n" +
                "  ports:\n" +
                "    Washington: 'x:0.0,y:0.0'\n" +
                "    Italy: '7.0,2.0'",
            PortListFactory.class
        );


        Assertions.assertEquals(factory.getPorts().size(), 2);
        final PortListInitializer initializer = factory.apply(mock(FishState.class));

        Assertions.assertEquals((int) initializer.getPorts().get("Washington").x, 0);
        Assertions.assertEquals((int) initializer.getPorts().get("Italy").x, 7);

    }
}
