package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;

import java.util.LinkedHashMap;

public class CatchSamplerFixedSampleTest {


    @Test
    public void makeSureTheRightPopulationIsSampled() {

        //I can tell what is sampled because a tag is added!
        FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().clear();

        FisherDefinition fisherDefinition = new FisherDefinition();
        fisherDefinition.getInitialFishersPerPort().put("Port 0", 50);
        fisherDefinition.setTags("lame");
        scenario.getFisherDefinitions().add(fisherDefinition);

        FisherDefinition fisherDefinition2 = new FisherDefinition();
        fisherDefinition2.getInitialFishersPerPort().put("Port 0", 50);
        fisherDefinition2.setTags("cool");
        scenario.getFisherDefinitions().add(fisherDefinition2);


        SPRAgentBuilderSelectiveSampling spr = new SPRAgentBuilderSelectiveSampling();
        spr.setSurveyTag("surveyed");
        spr.setProbabilityOfSamplingEachTag(new LinkedHashMap<>());
        spr.getProbabilityOfSamplingEachTag().put("lame", 0d);
        spr.getProbabilityOfSamplingEachTag().put("cool", .5d);

        scenario.getPlugins().add(spr);


        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        int numberOfBoatsSampled = 0;
        for (Fisher fisher : state.getFishers()) {
            //we shouldn't sample lames
            if (fisher.getTags().contains("lame") &&
                fisher.getTags().contains("surveyed Species 0"))
                Assert.assertTrue(false);
            if (fisher.getTags().contains("cool") &&
                fisher.getTags().contains("surveyed Species 0"))
                numberOfBoatsSampled++;


        }

        Assert.assertTrue(numberOfBoatsSampled > 5);
        Assert.assertTrue(numberOfBoatsSampled < 45);


    }

}