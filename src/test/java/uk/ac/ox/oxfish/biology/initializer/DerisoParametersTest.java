package uk.ac.ox.oxfish.biology.initializer;

import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

import static org.junit.Assert.*;

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