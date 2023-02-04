/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;

/**
 * its delegate is a market, but it only instantiates it (and start it)  at start or when it is given the species
 * it is supposed to study by the market map.
 * This class exists because the way market initialize is quite dumb since factories cannot be told what species they
 * need to create their market for. If we ever bother making a better initialization process for markets, this class
 * should probably die off
 */
public class MarketProxy implements Market{


    private Market delegate = null;

    /**
     * price map containing species --> prices factory
     */
    private Map<String, AlgorithmFactory<? extends Market>> pricesMap;


    /**
     * if this is set, ignore the price map; call the same algorithm factory every time
     */
    private AlgorithmFactory<? extends Market>  overrideMarketMaker;


    private Species species;

    private FishState state;

    public MarketProxy(
            Map<String,  AlgorithmFactory<? extends Market>> pricesMap) {
        this.pricesMap = pricesMap;
    }

    public MarketProxy(AlgorithmFactory<? extends Market> overrideMarketMaker) {
        this.overrideMarketMaker = overrideMarketMaker;
    }

    @Override
    public Species getSpecies() {
        return delegate.getSpecies();
    }

    @Override
    public void setSpecies(Species species) {

        Preconditions.checkArgument(delegate==null, "There already exist a market for " + species);
        Preconditions.checkArgument(species!=null, "You have already assigned a species for this market! ");
        this.species = species;
        initializeDelegateIfPossible();





    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        this.state=model;
        initializeDelegateIfPossible();
    }


    /**
     * Sells the a specific amount of fish here
     *
     * @param hold       the cargo of the agent
     * @param fisher     the seller
     * @param regulation the regulation object the seller abides to
     * @param state      the model
     * @param species    the species of fish we want to sell
     */
    @Override
    public TradeInfo sellFish(
            Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species) {

        return delegate.sellFish(hold, fisher, regulation, state, species);
    }

    /**
     * get the daily data of this market
     *
     * @return the time series object containing the daily data of the delegate
     */
    @Override
    public TimeSeries<Market> getData() {
        return delegate.getData();
    }

    /**
     * how much do you intend to pay the next epsilon amount of biomass sold here
     *
     * @return price
     */
    @Override
    public double getMarginalPrice() {
        if(delegate==null)
            return Double.NaN;
        return delegate.getMarginalPrice();
    }

    @Override
    public boolean isStarted() {
        return state != null;
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
        state =null;

    }

    private void initializeDelegateIfPossible(){

        //you can only start the delegate when you have both been started and given a species to focus on
        if(species== null || state == null)
            return;

        String speciesName = species.getName();
        if(overrideMarketMaker != null)
        {
            delegate = overrideMarketMaker.apply(state);
            delegate.setSpecies(species);
            delegate.start(state);
        }
        else {
            AlgorithmFactory<? extends Market> factory = pricesMap.get(speciesName);
            Preconditions.checkArgument(factory != null, "Can't create a market for " + species);
            delegate = factory.apply(state);
            delegate.setSpecies(species);
            delegate.start(state);
        }
    }


    public Market getDelegate() {
        return delegate;
    }
}
