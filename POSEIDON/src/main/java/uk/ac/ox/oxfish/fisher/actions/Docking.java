/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.TradeInfo;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.logging.Logger;

/**
 * Landing, selling all the hold and docking
 * Created by carrknight on 5/4/15.
 */
public class Docking implements Action {


    /**
     * dock to port and sell catch
     *
     * @param model      a link to the model, in case you need to grab global objects
     * @param agent      a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation governing the agent
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(
        final FishState model, final Fisher agent, final Regulation regulation, final double hoursLeft
    ) {

        final Port port = agent.getHomePort();
        assert agent.getLocation().equals(port.getLocation());
        assert agent.isAtDestination();
        assert !port.isDocked(agent); //shouldn't have docked already!


        final GlobalBiology biology = model.getBiology();

        final MarketMap marketMap = port.getMarketMap(agent);
        for (final Species species : biology.getSpecies()) {


            if (agent.getTotalWeightOfCatchInHold(species) > 0) {
                //this should take care of everything including transferring cash
                final TradeInfo tradeInfo = marketMap.sellFish(agent.getHold(), species, agent, regulation, model);
                //bean counting happens here:
                agent.processTradeData(tradeInfo);
            }
        }

        //sell your stuff
        final Catch toSell = agent.unload();
        //log it
        Logger.getGlobal().fine(() -> agent + " returns to port with catch: " + toSell);
        //anchor/refill
        agent.dock();

        assert agent.getLocation().equals(port.getLocation());
        assert agent.isAtDestination();
        assert port.isDocked(agent); //shouldn't have docked already!
        assert agent.getTotalWeightOfCatchInHold() == 0.0;
        //now stay at port
        return new ActionResult(new AtPort(), hoursLeft);
    }
}
