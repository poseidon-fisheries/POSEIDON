/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.Collection;

/**
 * A simple map Species ---> Market
 * Created by carrknight on 5/3/15.
 */
public class MarketMap {

    private final Market[] marketList;

    public MarketMap(GlobalBiology biology)
    {
        marketList = new Market[biology.getSize()];
    }

    public void addMarket(Species species, Market market)
    {
        Preconditions.checkArgument( marketList[species.getIndex()]==null);
        marketList[species.getIndex()]=market;
        market.setSpecies(species);
    }

    public Market getMarket(Species species)
    {
        return marketList[species.getIndex()];
    }


    public TradeInfo sellFish(
            Hold hold, Species species, Fisher fisher,
            Regulation regulation, FishState state)
    {
        return marketList[species.getIndex()].sellFish(hold, fisher, regulation, state, species);

    }
    
    public double getSpeciesPrice(int speciesIndex){
    	return marketList[speciesIndex].getMarginalPrice();
    }


    public Collection<Market> getMarkets()
    {
        return Arrays.asList(marketList);
    }



}
