package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;

import java.util.LinkedHashMap;
import java.util.function.Predicate;

public class SprAgentBuilderSelectiveSampling {


    @Test
    public void makeSureTheRightPopulationIsSampled() {

        FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().clear();

        FisherDefinition fisherDefinition = new FisherDefinition();
        fisherDefinition.getInitialFishersPerPort().put("Port 0",15);
        fisherDefinition.setTags("lame");
        scenario.getFisherDefinitions().add(fisherDefinition);

        FisherDefinition fisherDefinition2 = new FisherDefinition();
        fisherDefinition2.getInitialFishersPerPort().put("Port 0",15);
        fisherDefinition2.setTags("cool");
        scenario.getFisherDefinitions().add(fisherDefinition2);


        SPRAgentBuilderFixedSample spr = new SPRAgentBuilderFixedSample();
        spr.setSurveyTag("surveyed");
        spr.getTagsToSample().clear();
        spr.getTagsToSample().put("cool",10);
        spr.getTagsToSample().put("lame",5);

        scenario.getPlugins().add(spr);


        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        state.schedule.step(state);

        //look for the SPR agent (ugly)
        final SPRAgent agent = (SPRAgent) state.viewStartables().stream().filter(
                startable -> startable instanceof SPRAgent
        ).findFirst().get();

        int cools = 0;
        int lames = 0;
        for (Fisher fisher : agent.monitorObservedFishers()) {
            //we shouldn't sample lames
            if(fisher.getTags().contains("lame"))
                lames++;
            if(fisher.getTags().contains("cool"))
                cools++;


        }
        final Fisher firstMonitored = agent.monitorObservedFishers().get(0);
        final Fisher secondMonitored = agent.monitorObservedFishers().get(1);
        //force the first fisher to quit
        firstMonitored.setDepartingStrategy(new FixedProbabilityDepartingStrategy(0,true));

        Assert.assertEquals(lames,5);
        Assert.assertEquals(cools,10);

        //run for a year
        for(int i =0; i<400; i++)
            state.schedule.step(state);


        cools = 0;
        lames = 0;
        for (Fisher fisher : agent.monitorObservedFishers()) {
            //we shouldn't sample lames
            if(fisher.getTags().contains("lame"))
                lames++;
            if(fisher.getTags().contains("cool"))
                cools++;


        }
        Assert.assertEquals(lames,5);
        Assert.assertEquals(cools,10);

        //second fisher hasn't quit: should still be in
        Assert.assertTrue(agent.monitorObservedFishers().contains(secondMonitored));
        //first fisher was active ON THE FIRST DAY: should still be in!
        Assert.assertTrue(agent.monitorObservedFishers().contains(firstMonitored));

        //run it one more year
        for(int i =0; i<365; i++)
            state.schedule.step(state);

        //second fisher hasn't quit: should still be in
        Assert.assertTrue(agent.monitorObservedFishers().contains(secondMonitored));
        //first fisher wasn't active: should be out!
        Assert.assertFalse(agent.monitorObservedFishers().contains(firstMonitored));

    }

}
