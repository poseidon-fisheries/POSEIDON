package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.OsmoseWFSScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Paths;

/**
 * Created by carrknight on 12/6/16.
 */
public class FloridaLogitDestinationFactoryTest {


    @Test
    public void florida() throws Exception {

        FloridaLogitDestinationFactory factory = new FloridaLogitDestinationFactory();
        factory.setCoefficientsStandardDeviationFile(Paths.get("temp_wfs","longline_dummy.csv").toAbsolutePath().toString());
        FishState state = new FishState(1l);
        OsmoseWFSScenario scenario = new OsmoseWFSScenario();
        scenario.setLonglinerDestinationStrategy(factory);
        state.setScenario(scenario);
        state.start();
        LogitDestinationStrategy apply = factory.apply(state);
        System.out.println(FishStateUtilities.deepToStringArray(apply.getClassifier().getBetas(),",","\n"));


        Assert.assertEquals(2.346,apply.getClassifier().getBetas()[apply.getSwitcher().getArm(12)][3],.001);
    }
}