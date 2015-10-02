package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;


public class RespondToFines {


    //if there is fish that carries a fine, ignore it!
    @Test
    public void avoidFinedFish()
    {


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setRegulation(new AnarchyFactory());
        scenario.setFishers(50);
        //specie 1 is available only on the bottom half
        scenario.setBiologyInitializer(new HalfBycatchFactory());

        FishState state = new FishState(System.currentTimeMillis(), 1);
        state.setScenario(scenario);
        state.start();
        //make fishing specie 1 very expensive
        ((FixedPriceMarket) state.getPorts().iterator().next().getMarket(state.getBiology().getSpecie(1))).setPrice(-50);

        //run it for 1000 steps
        for(int i=0; i<1000; i++)
            state.schedule.step(state);

        //now check the hotspots. Lower hotspots should not be more than 5%
        //now check the hotspots
        double allHotspots = 0;
        double lowerHotSpots = 0;
        IntGrid2D hotspots = state.getMap().getDailyTrawlsMap();
        for(int x =0; x<state.getMap().getWidth(); x++)
        {
            for (int y = 0; y < state.getMap().getHeight(); y++)
            {
                double hotspot = hotspots.get(x, y);
                allHotspots += hotspot;
                //down on the gui is up on the grid since the top left corner is the 0,0
                if(y>state.getMap().getHeight()/2+1)
                    lowerHotSpots += hotspot;


            }

        }

        //on the line fishing make up at least 50% of all recent fishing
        System.out.println(allHotspots + " --- " + lowerHotSpots);
        System.out.println("percentage fished on the bottom : " + lowerHotSpots/allHotspots);
        Assert.assertTrue(allHotspots * .05 >= lowerHotSpots);
        Assert.assertTrue(allHotspots > 0);
    }

}
