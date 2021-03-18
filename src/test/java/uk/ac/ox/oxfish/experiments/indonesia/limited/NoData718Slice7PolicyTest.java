package uk.ac.ox.oxfish.experiments.indonesia.limited;

import org.junit.Test;
import uk.ac.ox.oxfish.model.data.webviz.scenarios.Scenario;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class NoData718Slice7PolicyTest {


    @Test
    public void lbspr() throws FileNotFoundException {

        Path toTest = Paths.get("inputs","tests","slice718","lbspr_test.yaml");
        FishYAML yaml = new FishYAML();




    }
}