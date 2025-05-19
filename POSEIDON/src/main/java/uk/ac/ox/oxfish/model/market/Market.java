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

package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * The market for a specie of fish
 * Created by carrknight on 5/3/15.
 */
public interface Market extends Startable {


    /**
     * does some standard stuff relating to selling fish:
     * 1) finds out how much is actually legal to sell
     * 2) give fisher money
     * 3) tell regulation
     * notice that it doesn't tell fisher to clear its hold, that should be the fisher responsibility
     *
     * @param biomass          pounds of fish to sell
     * @param fisher           the seller
     * @param regulation       the regulation object the seller abides to
     * @param state            the model
     * @param biomassToRevenue a function to find out how much money does the biomass sold actually command
     * @return biomass actually sold
     */
    static TradeInfo defaultMarketTransaction(
        final double biomass, final Fisher fisher, final Regulation regulation,
        final FishState state, final Function<Double, Double> biomassToRevenue, final Species species
    ) {

        //find out legal biomass sold
        final double biomassActuallySellable = Math.min(
            biomass,
            regulation.maximumBiomassSellable(fisher, species, state)
        );

        if (biomassActuallySellable < biomass)
            Logger.getGlobal().fine("Regulations allow only " + biomassActuallySellable + " to be sold by " + fisher);

        if (biomassActuallySellable <= 0)
            return new TradeInfo(0, species, 0);


        final double revenue = biomassToRevenue.apply(biomassActuallySellable);

        //give fisher the money
        fisher.earn(revenue);

        //tell regulation
        regulation.reactToSale(species, fisher, biomassActuallySellable, revenue, state);

        //return biomass sellable
        return new TradeInfo(biomassActuallySellable, species, revenue);

    }

    /**
     * Sells the a specific amount of fish here
     *
     * @param hold       the cargo of the agent
     * @param fisher     the seller
     * @param regulation the regulation object the seller abides to
     * @param state      the model
     */
    TradeInfo sellFish(
        Hold hold,
        Fisher fisher,
        Regulation regulation, FishState state,
        Species species
    );

    /**
     * get the daily data of this market
     *
     * @return
     */
    TimeSeries<Market> getData();

    /**
     * how much do you intend to pay the next epsilon amount of biomass sold here
     *
     * @return price
     */
    double getMarginalPrice();


    Species getSpecies();

    void setSpecies(Species species);

    boolean isStarted();


}
