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