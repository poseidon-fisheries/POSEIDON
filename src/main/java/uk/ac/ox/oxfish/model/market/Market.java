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

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.function.Function;

/**
 * The market for a specie of fish
 * Created by carrknight on 5/3/15.
 */
public interface Market extends Startable {





    /**
     * Sells the a specific amount of fish here
     * @param hold the cargo of the agent
     * @param fisher the seller
     * @param regulation the regulation object the seller abides to
     * @param state the model
     */
    TradeInfo sellFish(
            Hold hold,
            Fisher fisher,
            Regulation regulation, FishState state,
            Species species);


    /**
     * does some standard stuff relating to selling fish:
     * 1) finds out how much is actually legal to sell
     * 2) give fisher money
     * 3) tell regulation
     * notice that it doesn't tell fisher to clear its hold, that should be the fisher responsibility
     * @param biomass pounds of fish to sell
     * @param fisher the seller
     * @param regulation the regulation object the seller abides to
     * @param state the model
     * @param biomassToRevenue a function to find out how much money does the biomass sold actually command
     * @return biomass actually sold
     */
    static TradeInfo defaultMarketTransaction(
            double biomass, Fisher fisher, Regulation regulation,
            FishState state, Function<Double, Double> biomassToRevenue, Species species
    )
    {

        //find out legal biomass sold
        double biomassActuallySellable = Math.min(biomass,
                                                  regulation.maximumBiomassSellable(fisher, species, state));

        if(Log.TRACE && biomassActuallySellable < biomass)
            Log.trace("Regulations allow only " + biomassActuallySellable + " to be sold by " + fisher);

        if(biomassActuallySellable <=0)
            return new TradeInfo(0, species, 0);



        double revenue = biomassToRevenue.apply(biomassActuallySellable);

        //give fisher the money
        fisher.earn(revenue);

        //tell regulation
        regulation.reactToSale(species, fisher , biomassActuallySellable, revenue);

        //return biomass sellable
        return new TradeInfo(biomassActuallySellable, species, revenue);

    }


    /**
     * get the daily data of this market
     * @return
     */
    TimeSeries<Market> getData();

    /**
     * how much do you intend to pay the next epsilon amount of biomass sold here
     * @return price
     */
    double getMarginalPrice();


    public Species getSpecies();

    public void setSpecies(Species species);

    public boolean isStarted();



}
