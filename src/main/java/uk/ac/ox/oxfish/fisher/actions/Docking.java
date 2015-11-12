package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.TradeInfo;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Landing, selling all the hold and docking
 * Created by carrknight on 5/4/15.
 */
public class Docking implements Action{


    /**
     * dock to port and sell catch
     *
     * @param model       a link to the model, in case you need to grab global objects
     * @param agent       a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation governing the agent
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(
            FishState model, Fisher agent, Regulation regulation, double hoursLeft)
    {

        Port port = agent.getHomePort();
        assert agent.getLocation().equals(port.getLocation());
        assert agent.isAtDestination();
        assert !port.isDocked(agent); //shouldn't have docked already!



        //sell your stuff
        Catch toSell = agent.unload();
        //log it

        GlobalBiology biology = model.getBiology();

        MarketMap marketMap =port.marketMap();
        for(Species species : biology.getSpecies())
        {
            double biomass = toSell.getPoundsCaught(species);
            assert  biomass>=0;
            if(biomass>0) {
                //this should take care of everything including transferring cash
                TradeInfo tradeInfo = marketMap.sellFish(species, biomass, agent, regulation, model);
                //bean counting happens here:
                agent.processTradeData(tradeInfo);
            }
        }

        //anchor/refill
        agent.dock();

        assert agent.getLocation().equals(port.getLocation());
        assert agent.isAtDestination();
        assert port.isDocked(agent); //shouldn't have docked already!
        assert agent.getPoundsCarried() ==  0.0;
        //now stay at port
        return new ActionResult(new AtPort(),hoursLeft);
    }
}
