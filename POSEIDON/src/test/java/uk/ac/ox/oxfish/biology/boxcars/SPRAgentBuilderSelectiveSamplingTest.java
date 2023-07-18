package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;

import java.util.LinkedHashMap;

public class SPRAgentBuilderSelectiveSamplingTest {


    @Test
    public void makeSureTheRightPopulationIsSampled() {

        //I can tell what is sampled because a tag is added!
        final FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().clear();

        final FisherDefinition fisherDefinition = new FisherDefinition();
        fisherDefinition.getInitialFishersPerPort().put("Port 0", 50);
        fisherDefinition.setTags("lame");
        scenario.getFisherDefinitions().add(fisherDefinition);

        final FisherDefinition fisherDefinition2 = new FisherDefinition();
        fisherDefinition2.getInitialFishersPerPort().put("Port 0", 50);
        fisherDefinition2.setTags("cool");
        scenario.getFisherDefinitions().add(fisherDefinition2);


        final SPRAgentBuilderSelectiveSampling spr = new SPRAgentBuilderSelectiveSampling();
        spr.setSurveyTag("surveyed");
        spr.setProbabilityOfSamplingEachTag(new LinkedHashMap<>());
        spr.getProbabilityOfSamplingEachTag().put("lame", 0d);
        spr.getProbabilityOfSamplingEachTag().put("cool", .5d);

        scenario.getPlugins().add(spr);


        final FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        int numberOfBoatsSampled = 0;
        for (final Fisher fisher : state.getFishers()) {
            //we shouldn't sample lames
            if (fisher.getTagsList().contains("lame") &&
                fisher.getTagsList().contains("surveyed Species 0"))
                Assertions.fail();
            if (fisher.getTagsList().contains("cool") &&
                fisher.getTagsList().contains("surveyed Species 0"))
                numberOfBoatsSampled++;


        }

        Assertions.assertTrue(numberOfBoatsSampled > 5);
        Assertions.assertTrue(numberOfBoatsSampled < 45);


    }
}