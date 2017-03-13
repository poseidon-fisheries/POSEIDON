package uk.ac.ox.oxfish.geography.ports;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.*;

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
                             "    Washington: '0.0,0.0'\n" +
                             "    Italy: '5.0,2.0'"
                     );

    }


    @Test
    public void yamlIn() throws Exception {


        FishYAML yaml = new FishYAML();
        PortListFactory factory = yaml.loadAs("List of Ports:\n" +
                                       "  ports:\n" +
                                       "    Washington: 0.0,0.0\n" +
                                       "    Italy: 7.0,2.0",
                               PortListFactory.class);

        assertEquals(factory.getPorts().size(),2);
        assertEquals((int)factory.getPorts().get("Italy").x,7);

    }
}