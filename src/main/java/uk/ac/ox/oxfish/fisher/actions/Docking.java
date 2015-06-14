package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Markets;
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
            FishState model, Fisher agent, Regulation regulation)
    {

        Port port = agent.getHomePort();
        assert agent.getLocation().equals(port.getLocation());
        assert agent.isAtDestination();
        assert !port.isDocked(agent); //shouldn't have docked already!

        //land
        port.dock(agent);

        //now sell
        Catch toSell = agent.unload();
        GlobalBiology biology = model.getBiology();

        Markets markets =port.getMarkets();
        for(Specie specie : biology.getSpecies())
        {
            double biomass = toSell.getPoundsCaught(specie);
            assert  biomass>=0;
            if(biomass>0)
                //this should take care of everything
                markets.getMarket(specie).sellFish(biomass,agent, regulation,model);
        }

        assert agent.getLocation().equals(port.getLocation());
        assert agent.isAtDestination();
        assert port.isDocked(agent); //shouldn't have docked already!
        assert agent.getPoundsCarried() ==  0.0;
        //now stay at port
        return new ActionResult(new AtPort(),false);
    }
}
